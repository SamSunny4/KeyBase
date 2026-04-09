package src;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class BackupManager {
    public static final String BACKUP_EXTENSION = ".kb.bak";
    public static final String DEFAULT_BACKUP_FILE_NAME = "keybase" + BACKUP_EXTENSION;

    private static final String FILE_MAGIC = "KEYBASE_BACKUP";
    private static final int FILE_VERSION = 1;
    private static final int ENTRY_MARKER = 0x4B42454E; // KBEN
    private static final int PAYLOAD_VERSION = 1;
    private static final int HASH_LENGTH_BYTES = 32; // SHA-256
    private static final int MAX_PAYLOAD_BYTES = 256 * 1024 * 1024;
    private static final Pattern SAFE_EXTENSION = Pattern.compile("\\.[a-z0-9]{1,8}");

    private BackupManager() {
    }

    public static BackupAppendResult appendIncrementalBackup(File backupFile) throws IOException, SQLException {
        validateBackupPath(backupFile, false);

        ParsedBackup existing = ParsedBackup.empty();
        if (backupFile.exists() && backupFile.length() > 0) {
            existing = readBackupFile(backupFile, false, false);
        }

        Snapshot snapshot = collectCurrentDataSnapshot(existing.fingerprints);
        if (snapshot.records.isEmpty()) {
            return new BackupAppendResult(backupFile.getAbsoluteFile(), 0, 0, existing.entryCount);
        }

        byte[] payload = encodeSnapshot(snapshot);
        byte[] payloadHash = sha256(payload);

        boolean writeHeader = !backupFile.exists() || backupFile.length() == 0;
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(backupFile, true)))) {
            if (writeHeader) {
                out.writeUTF(FILE_MAGIC);
                out.writeInt(FILE_VERSION);
            }
            writeEntry(out, System.currentTimeMillis(), snapshot.records.size(), snapshot.images.size(), payload, payloadHash);
        }

        return new BackupAppendResult(
            backupFile.getAbsoluteFile(),
            snapshot.records.size(),
            snapshot.images.size(),
            existing.entryCount + 1
        );
    }

    public static RestoreResult restoreMissingData(File backupFile) throws IOException, SQLException {
        validateBackupPath(backupFile, true);
        if (!backupFile.canRead()) {
            throw new IOException("Backup file is not readable: " + backupFile.getAbsolutePath());
        }

        ParsedBackup parsed = readBackupFile(backupFile, true, true);
        if (parsed.entryCount == 0) {
            throw new IOException("Backup file does not contain any snapshots.");
        }

        List<BackupRecord> orderedUniqueRecords = new ArrayList<>();
        Set<String> seenBackupFingerprints = new HashSet<>();
        Map<String, ImageAsset> imageAssets = new LinkedHashMap<>();

        for (Snapshot snapshot : parsed.snapshots) {
            for (Map.Entry<String, ImageAsset> imageEntry : snapshot.images.entrySet()) {
                imageAssets.putIfAbsent(imageEntry.getKey(), imageEntry.getValue());
            }
            for (BackupRecord record : snapshot.records) {
                if (seenBackupFingerprints.add(record.fingerprint)) {
                    orderedUniqueRecords.add(record);
                }
            }
        }

        File imageDirectory = new File(AppConfig.getImagesDirectory());
        if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
            throw new IOException("Unable to create image directory: " + imageDirectory.getAbsolutePath());
        }
        if (!imageDirectory.isDirectory()) {
            throw new IOException("Image path is not a directory: " + imageDirectory.getAbsolutePath());
        }

        int insertedRecords = 0;
        int skippedRecords = 0;
        int[] restoredImageCount = new int[] {0};

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean hasTimeColumn = hasColumn(conn, "DUPLICATOR", "TIME_ADDED");
            Set<String> existingFingerprints = loadDatabaseFingerprints(conn, hasTimeColumn);

            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String insertSql = hasTimeColumn
                ? "INSERT INTO duplicator (name, phone_number, id_no, vehicle_no, key_no, key_type, purpose, date_added, time_added, remarks, quantity, amount, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                : "INSERT INTO duplicator (name, phone_number, id_no, vehicle_no, key_no, key_type, purpose, date_added, remarks, quantity, amount, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            Map<String, String> restoredImagePaths = new LinkedHashMap<>();

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (BackupRecord record : orderedUniqueRecords) {
                    if (existingFingerprints.contains(record.fingerprint)) {
                        skippedRecords++;
                        continue;
                    }

                    String restoredImagePath = ensureImagePresent(
                        record.imageHash,
                        imageAssets,
                        imageDirectory,
                        restoredImagePaths,
                        restoredImageCount
                    );

                    bindInsertStatement(insertStmt, record, restoredImagePath, hasTimeColumn);
                    insertStmt.executeUpdate();

                    existingFingerprints.add(record.fingerprint);
                    insertedRecords++;
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof IOException) {
                    throw (IOException) ex;
                }
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Restore failed: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }

        return new RestoreResult(
            backupFile.getAbsoluteFile(),
            insertedRecords,
            skippedRecords,
            restoredImageCount[0],
            parsed.entryCount
        );
    }

    public static void verifyBackupFileIntegrity(File backupFile) throws IOException {
        validateBackupPath(backupFile, true);
        readBackupFile(backupFile, false, false);
    }

    private static Snapshot collectCurrentDataSnapshot(Set<String> alreadyBackedUpFingerprints) throws SQLException {
        Snapshot snapshot = new Snapshot();

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean hasTimeColumn = hasColumn(conn, "DUPLICATOR", "TIME_ADDED");
            String selectSql = hasTimeColumn
                ? "SELECT name, phone_number, id_no, vehicle_no, key_no, key_type, purpose, date_added, time_added, remarks, quantity, amount, image_path FROM duplicator ORDER BY duplicator_id ASC"
                : "SELECT name, phone_number, id_no, vehicle_no, key_no, key_type, purpose, date_added, remarks, quantity, amount, image_path FROM duplicator ORDER BY duplicator_id ASC";

            try (PreparedStatement stmt = conn.prepareStatement(selectSql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = required(rs.getString("name"));
                    String phone = required(rs.getString("phone_number"));
                    String idNo = required(rs.getString("id_no"));

                    String vehicleNo = trimToNull(rs.getString("vehicle_no"));
                    String keyNo = trimToNull(rs.getString("key_no"));
                    String keyType = trimToNull(rs.getString("key_type"));
                    String purpose = trimToNull(rs.getString("purpose"));

                    java.sql.Date sqlDate = rs.getDate("date_added");
                    String dateAdded = sqlDate != null ? sqlDate.toString() : null;

                    String timeAdded = null;
                    if (hasTimeColumn) {
                        java.sql.Time sqlTime = rs.getTime("time_added");
                        timeAdded = sqlTime != null ? sqlTime.toString() : null;
                    }

                    String remarks = trimToNull(rs.getString("remarks"));
                    int quantity = rs.getInt("quantity");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    if (amount == null) {
                        amount = BigDecimal.ZERO;
                    }

                    String fingerprint = buildFingerprint(
                        name,
                        phone,
                        idNo,
                        vehicleNo,
                        keyNo,
                        keyType,
                        purpose,
                        dateAdded,
                        timeAdded,
                        remarks,
                        quantity,
                        amount
                    );

                    if (alreadyBackedUpFingerprints.contains(fingerprint)) {
                        continue;
                    }

                    String imagePath = trimToNull(rs.getString("image_path"));
                    String imageHash = null;
                    if (imagePath != null) {
                        ImageAsset imageAsset = loadImageAsset(imagePath);
                        if (imageAsset != null) {
                            imageHash = imageAsset.hash;
                            snapshot.images.putIfAbsent(imageAsset.hash, imageAsset);
                        }
                    }

                    BackupRecord record = new BackupRecord(
                        fingerprint,
                        name,
                        phone,
                        idNo,
                        vehicleNo,
                        keyNo,
                        keyType,
                        purpose,
                        dateAdded,
                        timeAdded,
                        remarks,
                        quantity,
                        amount.setScale(2, RoundingMode.HALF_UP).doubleValue(),
                        imageHash
                    );
                    snapshot.records.add(record);
                }
            }
        }

        return snapshot;
    }

    private static Set<String> loadDatabaseFingerprints(Connection conn, boolean hasTimeColumn) throws SQLException {
        Set<String> fingerprints = new HashSet<>();

        String selectSql = hasTimeColumn
            ? "SELECT name, phone_number, id_no, vehicle_no, key_no, key_type, purpose, date_added, time_added, remarks, quantity, amount FROM duplicator"
            : "SELECT name, phone_number, id_no, vehicle_no, key_no, key_type, purpose, date_added, remarks, quantity, amount FROM duplicator";

        try (PreparedStatement stmt = conn.prepareStatement(selectSql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = required(rs.getString("name"));
                String phone = required(rs.getString("phone_number"));
                String idNo = required(rs.getString("id_no"));

                String vehicleNo = trimToNull(rs.getString("vehicle_no"));
                String keyNo = trimToNull(rs.getString("key_no"));
                String keyType = trimToNull(rs.getString("key_type"));
                String purpose = trimToNull(rs.getString("purpose"));

                java.sql.Date sqlDate = rs.getDate("date_added");
                String dateAdded = sqlDate != null ? sqlDate.toString() : null;

                String timeAdded = null;
                if (hasTimeColumn) {
                    java.sql.Time sqlTime = rs.getTime("time_added");
                    timeAdded = sqlTime != null ? sqlTime.toString() : null;
                }

                String remarks = trimToNull(rs.getString("remarks"));
                int quantity = rs.getInt("quantity");
                BigDecimal amount = rs.getBigDecimal("amount");
                if (amount == null) {
                    amount = BigDecimal.ZERO;
                }

                String fingerprint = buildFingerprint(
                    name,
                    phone,
                    idNo,
                    vehicleNo,
                    keyNo,
                    keyType,
                    purpose,
                    dateAdded,
                    timeAdded,
                    remarks,
                    quantity,
                    amount
                );

                fingerprints.add(fingerprint);
            }
        }

        return fingerprints;
    }

    private static ImageAsset loadImageAsset(String imagePath) {
        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(imageFile.toPath());
            if (bytes.length == 0) {
                return null;
            }
            String hash = toHex(sha256(bytes));
            String extension = sanitizeExtension(extractExtension(imageFile.getName()));
            return new ImageAsset(hash, extension, bytes);
        } catch (IOException ex) {
            return null;
        }
    }

    private static byte[] encodeSnapshot(Snapshot snapshot) throws IOException {
        ByteArrayOutputStream rawOutput = new ByteArrayOutputStream();

        try (GZIPOutputStream gzipOutput = new GZIPOutputStream(rawOutput);
             DataOutputStream out = new DataOutputStream(gzipOutput)) {

            out.writeInt(PAYLOAD_VERSION);

            out.writeInt(snapshot.records.size());
            for (BackupRecord record : snapshot.records) {
                writeNullableString(out, record.fingerprint);
                writeNullableString(out, record.name);
                writeNullableString(out, record.phoneNumber);
                writeNullableString(out, record.idNo);
                writeNullableString(out, record.vehicleNo);
                writeNullableString(out, record.keyNo);
                writeNullableString(out, record.keyType);
                writeNullableString(out, record.purpose);
                writeNullableString(out, record.dateAdded);
                writeNullableString(out, record.timeAdded);
                writeNullableString(out, record.remarks);
                out.writeInt(record.quantity);
                out.writeDouble(record.amount);
                writeNullableString(out, record.imageHash);
            }

            out.writeInt(snapshot.images.size());
            for (Map.Entry<String, ImageAsset> imageEntry : snapshot.images.entrySet()) {
                ImageAsset asset = imageEntry.getValue();
                writeNullableString(out, asset.hash);
                writeNullableString(out, asset.extension);
                out.writeInt(asset.bytes.length);
                out.write(asset.bytes);
            }
        }

        return rawOutput.toByteArray();
    }

    private static Snapshot decodeSnapshot(byte[] payload, boolean includeImageBytes) throws IOException {
        Snapshot snapshot = new Snapshot();

        try (DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(payload)))) {
            int payloadVersion = in.readInt();
            if (payloadVersion != PAYLOAD_VERSION) {
                throw new IOException("Unsupported snapshot payload version: " + payloadVersion);
            }

            int recordCount = in.readInt();
            if (recordCount < 0) {
                throw new IOException("Invalid record count in snapshot payload.");
            }

            for (int i = 0; i < recordCount; i++) {
                String fingerprint = trimToNull(readNullableString(in));
                String name = trimToNull(readNullableString(in));
                String phoneNumber = trimToNull(readNullableString(in));
                String idNo = trimToNull(readNullableString(in));
                String vehicleNo = trimToNull(readNullableString(in));
                String keyNo = trimToNull(readNullableString(in));
                String keyType = trimToNull(readNullableString(in));
                String purpose = trimToNull(readNullableString(in));
                String dateAdded = trimToNull(readNullableString(in));
                String timeAdded = trimToNull(readNullableString(in));
                String remarks = trimToNull(readNullableString(in));
                int quantity = in.readInt();
                double amount = in.readDouble();
                String imageHash = trimToNull(readNullableString(in));

                if (fingerprint == null) {
                    fingerprint = buildFingerprint(
                        name,
                        phoneNumber,
                        idNo,
                        vehicleNo,
                        keyNo,
                        keyType,
                        purpose,
                        dateAdded,
                        timeAdded,
                        remarks,
                        quantity,
                        BigDecimal.valueOf(amount)
                    );
                }

                BackupRecord record = new BackupRecord(
                    fingerprint,
                    name,
                    phoneNumber,
                    idNo,
                    vehicleNo,
                    keyNo,
                    keyType,
                    purpose,
                    dateAdded,
                    timeAdded,
                    remarks,
                    quantity,
                    amount,
                    imageHash
                );
                snapshot.records.add(record);
            }

            int imageCount = in.readInt();
            if (imageCount < 0) {
                throw new IOException("Invalid image count in snapshot payload.");
            }

            for (int i = 0; i < imageCount; i++) {
                String hash = trimToNull(readNullableString(in));
                String extension = trimToNull(readNullableString(in));
                int length = in.readInt();
                if (length < 0 || length > MAX_PAYLOAD_BYTES) {
                    throw new IOException("Invalid image payload length: " + length);
                }

                byte[] bytes = in.readNBytes(length);
                if (bytes.length != length) {
                    throw new IOException("Snapshot payload ended unexpectedly while reading image bytes.");
                }

                if (!includeImageBytes) {
                    continue;
                }

                if (hash == null) {
                    hash = toHex(sha256(bytes));
                }
                String normalizedExtension = sanitizeExtension(extension);
                snapshot.images.put(hash, new ImageAsset(hash, normalizedExtension, bytes));
            }
        }

        return snapshot;
    }

    private static ParsedBackup readBackupFile(File backupFile, boolean includeImageBytes, boolean keepSnapshots) throws IOException {
        if (!backupFile.exists() || backupFile.length() == 0) {
            return ParsedBackup.empty();
        }

        ParsedBackup parsed = ParsedBackup.empty();

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(backupFile)))) {
            String magic = in.readUTF();
            if (!FILE_MAGIC.equals(magic)) {
                throw new IOException("Invalid backup file format. Expected KeyBase backup file.");
            }

            int version = in.readInt();
            if (version != FILE_VERSION) {
                throw new IOException("Unsupported backup file version: " + version);
            }

            while (true) {
                int marker;
                try {
                    marker = in.readInt();
                } catch (EOFException eof) {
                    break;
                }

                if (marker != ENTRY_MARKER) {
                    throw new IOException("Backup entry marker mismatch. File may be corrupted.");
                }

                long createdAt = in.readLong();
                int expectedRecordCount = in.readInt();
                int expectedImageCount = in.readInt();
                int payloadLength = in.readInt();

                if (payloadLength < 0 || payloadLength > MAX_PAYLOAD_BYTES) {
                    throw new IOException("Invalid snapshot payload size: " + payloadLength);
                }

                byte[] payload = in.readNBytes(payloadLength);
                if (payload.length != payloadLength) {
                    throw new IOException("Backup file ended unexpectedly while reading snapshot payload.");
                }

                int hashLength = in.readInt();
                if (hashLength != HASH_LENGTH_BYTES) {
                    throw new IOException("Unexpected snapshot hash length in backup file.");
                }

                byte[] expectedHash = in.readNBytes(hashLength);
                if (expectedHash.length != hashLength) {
                    throw new IOException("Backup file ended unexpectedly while reading snapshot hash.");
                }

                byte[] actualHash = sha256(payload);
                if (!Arrays.equals(expectedHash, actualHash)) {
                    throw new IOException("Backup integrity check failed for snapshot at " + createdAt + ".");
                }

                Snapshot decoded = decodeSnapshot(payload, includeImageBytes);
                if (decoded.records.size() != expectedRecordCount) {
                    throw new IOException("Record count mismatch in snapshot payload.");
                }
                if (includeImageBytes && decoded.images.size() != expectedImageCount) {
                    throw new IOException("Image count mismatch in snapshot payload.");
                }

                parsed.entryCount++;
                for (BackupRecord record : decoded.records) {
                    parsed.fingerprints.add(record.fingerprint);
                }

                if (keepSnapshots) {
                    parsed.snapshots.add(decoded);
                }
            }
        }

        return parsed;
    }

    private static void writeEntry(
        DataOutputStream out,
        long createdAt,
        int recordCount,
        int imageCount,
        byte[] payload,
        byte[] payloadHash
    ) throws IOException {
        out.writeInt(ENTRY_MARKER);
        out.writeLong(createdAt);
        out.writeInt(recordCount);
        out.writeInt(imageCount);
        out.writeInt(payload.length);
        out.write(payload);
        out.writeInt(payloadHash.length);
        out.write(payloadHash);
    }

    private static void bindInsertStatement(
        PreparedStatement stmt,
        BackupRecord record,
        String imagePath,
        boolean hasTimeColumn
    ) throws SQLException {
        stmt.clearParameters();

        stmt.setString(1, required(record.name));
        stmt.setString(2, required(record.phoneNumber));
        stmt.setString(3, required(record.idNo));

        setNullableString(stmt, 4, record.vehicleNo);
        setNullableString(stmt, 5, record.keyNo);
        setNullableString(stmt, 6, record.keyType);
        setNullableString(stmt, 7, record.purpose);

        java.sql.Date sqlDate = parseDate(record.dateAdded);

        if (hasTimeColumn) {
            if (sqlDate == null) {
                stmt.setNull(8, Types.DATE);
            } else {
                stmt.setDate(8, sqlDate);
            }

            java.sql.Time sqlTime = parseTime(record.timeAdded);
            if (sqlTime == null) {
                stmt.setNull(9, Types.TIME);
            } else {
                stmt.setTime(9, sqlTime);
            }

            setNullableString(stmt, 10, record.remarks);
            stmt.setInt(11, record.quantity);
            stmt.setBigDecimal(12, BigDecimal.valueOf(record.amount).setScale(2, RoundingMode.HALF_UP));
            setNullableString(stmt, 13, imagePath);
        } else {
            if (sqlDate == null) {
                stmt.setNull(8, Types.DATE);
            } else {
                stmt.setDate(8, sqlDate);
            }

            setNullableString(stmt, 9, record.remarks);
            stmt.setInt(10, record.quantity);
            stmt.setBigDecimal(11, BigDecimal.valueOf(record.amount).setScale(2, RoundingMode.HALF_UP));
            setNullableString(stmt, 12, imagePath);
        }
    }

    private static String ensureImagePresent(
        String imageHash,
        Map<String, ImageAsset> imageAssets,
        File imageDirectory,
        Map<String, String> restoredImagePaths,
        int[] restoredImageCount
    ) throws IOException {
        if (isBlank(imageHash)) {
            return null;
        }

        String normalizedHash = imageHash.trim().toLowerCase(Locale.ROOT);
        if (restoredImagePaths.containsKey(normalizedHash)) {
            return restoredImagePaths.get(normalizedHash);
        }

        ImageAsset asset = imageAssets.get(normalizedHash);
        if (asset == null) {
            // Fallback for non-normalized key.
            asset = imageAssets.get(imageHash);
        }
        if (asset == null) {
            return null;
        }

        String calculatedHash = toHex(sha256(asset.bytes));
        if (!normalizedHash.equals(calculatedHash)) {
            throw new IOException("Image payload hash mismatch while restoring backup.");
        }

        String extension = sanitizeExtension(asset.extension);
        File target = new File(imageDirectory, "restored_" + normalizedHash + extension);

        boolean wroteFile = false;
        if (target.exists()) {
            byte[] existingBytes = Files.readAllBytes(target.toPath());
            String existingHash = toHex(sha256(existingBytes));
            if (!normalizedHash.equals(existingHash)) {
                Files.write(target.toPath(), asset.bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                wroteFile = true;
            }
        } else {
            Files.write(target.toPath(), asset.bytes, StandardOpenOption.CREATE_NEW);
            wroteFile = true;
        }

        if (wroteFile) {
            restoredImageCount[0]++;
        }

        String absolutePath = target.getAbsolutePath();
        restoredImagePaths.put(normalizedHash, absolutePath);
        return absolutePath;
    }

    private static String buildFingerprint(
        String name,
        String phone,
        String idNo,
        String vehicleNo,
        String keyNo,
        String keyType,
        String purpose,
        String dateAdded,
        String timeAdded,
        String remarks,
        int quantity,
        BigDecimal amount
    ) {
        String canonical = String.join(
            "|",
            canonical(name),
            canonical(phone),
            canonical(idNo),
            canonical(vehicleNo),
            canonical(keyNo),
            canonical(keyType),
            canonical(purpose),
            canonical(dateAdded),
            canonical(timeAdded),
            canonical(remarks),
            Integer.toString(quantity),
            normalizeAmount(amount)
        );

        return toHex(sha256(canonical.getBytes(StandardCharsets.UTF_8)));
    }

    private static String normalizeAmount(BigDecimal amount) {
        BigDecimal normalized = amount == null ? BigDecimal.ZERO : amount;
        return normalized.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String table = tableName == null ? "" : tableName.toUpperCase(Locale.ROOT);
        String column = columnName == null ? "" : columnName.toUpperCase(Locale.ROOT);

        try (ResultSet columns = metaData.getColumns(null, null, table, column)) {
            return columns != null && columns.next();
        }
    }

    private static void validateBackupPath(File backupFile, boolean mustExist) throws IOException {
        if (backupFile == null) {
            throw new IOException("Backup file path is required.");
        }

        String lowerName = backupFile.getName().toLowerCase(Locale.ROOT);
        if (!lowerName.endsWith(BACKUP_EXTENSION)) {
            throw new IOException("Backup file must use the " + BACKUP_EXTENSION + " extension.");
        }

        File parent = backupFile.getAbsoluteFile().getParentFile();
        if (parent == null) {
            throw new IOException("Backup location is invalid.");
        }

        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Unable to create backup location: " + parent.getAbsolutePath());
        }

        if (!parent.isDirectory()) {
            throw new IOException("Backup location is not a directory: " + parent.getAbsolutePath());
        }

        if (!parent.canWrite()) {
            throw new IOException("Backup location is not writable: " + parent.getAbsolutePath());
        }

        if (mustExist && (!backupFile.exists() || !backupFile.isFile())) {
            throw new IOException("Backup file was not found: " + backupFile.getAbsolutePath());
        }
    }

    private static void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (isBlank(value)) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value.trim());
        }
    }

    private static java.sql.Date parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static java.sql.Time parseTime(String value) {
        if (isBlank(value)) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.length() > 8) {
            trimmed = trimmed.substring(0, 8);
        }

        try {
            return java.sql.Time.valueOf(trimmed);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String required(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String canonical(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void writeNullableString(DataOutputStream out, String value) throws IOException {
        if (value == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(value);
        }
    }

    private static String readNullableString(DataInputStream in) throws IOException {
        boolean present = in.readBoolean();
        if (!present) {
            return null;
        }
        return in.readUTF();
    }

    private static String extractExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return ".jpg";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".jpg";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private static String sanitizeExtension(String extension) {
        String candidate = extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
        if (candidate.isEmpty()) {
            return ".jpg";
        }

        if (!candidate.startsWith(".")) {
            candidate = "." + candidate;
        }

        if (SAFE_EXTENSION.matcher(candidate).matches()) {
            return candidate;
        }
        return ".jpg";
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available in this runtime.", ex);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static final class ParsedBackup {
        private int entryCount;
        private final Set<String> fingerprints;
        private final List<Snapshot> snapshots;

        private ParsedBackup() {
            this.entryCount = 0;
            this.fingerprints = new HashSet<>();
            this.snapshots = new ArrayList<>();
        }

        private static ParsedBackup empty() {
            return new ParsedBackup();
        }
    }

    private static final class Snapshot {
        private final List<BackupRecord> records;
        private final Map<String, ImageAsset> images;

        private Snapshot() {
            this.records = new ArrayList<>();
            this.images = new LinkedHashMap<>();
        }
    }

    private static final class BackupRecord {
        private final String fingerprint;
        private final String name;
        private final String phoneNumber;
        private final String idNo;
        private final String vehicleNo;
        private final String keyNo;
        private final String keyType;
        private final String purpose;
        private final String dateAdded;
        private final String timeAdded;
        private final String remarks;
        private final int quantity;
        private final double amount;
        private final String imageHash;

        private BackupRecord(
            String fingerprint,
            String name,
            String phoneNumber,
            String idNo,
            String vehicleNo,
            String keyNo,
            String keyType,
            String purpose,
            String dateAdded,
            String timeAdded,
            String remarks,
            int quantity,
            double amount,
            String imageHash
        ) {
            this.fingerprint = trimToNull(fingerprint);
            this.name = trimToNull(name);
            this.phoneNumber = trimToNull(phoneNumber);
            this.idNo = trimToNull(idNo);
            this.vehicleNo = trimToNull(vehicleNo);
            this.keyNo = trimToNull(keyNo);
            this.keyType = trimToNull(keyType);
            this.purpose = trimToNull(purpose);
            this.dateAdded = trimToNull(dateAdded);
            this.timeAdded = trimToNull(timeAdded);
            this.remarks = trimToNull(remarks);
            this.quantity = quantity;
            this.amount = amount;
            this.imageHash = trimToNull(imageHash);
        }
    }

    private static final class ImageAsset {
        private final String hash;
        private final String extension;
        private final byte[] bytes;

        private ImageAsset(String hash, String extension, byte[] bytes) {
            this.hash = hash == null ? "" : hash.toLowerCase(Locale.ROOT);
            this.extension = sanitizeExtension(extension);
            this.bytes = bytes == null ? new byte[0] : bytes;
        }
    }

    public static final class BackupAppendResult {
        private final File backupFile;
        private final int recordsAdded;
        private final int imagesAdded;
        private final int snapshotCount;

        private BackupAppendResult(File backupFile, int recordsAdded, int imagesAdded, int snapshotCount) {
            this.backupFile = backupFile;
            this.recordsAdded = recordsAdded;
            this.imagesAdded = imagesAdded;
            this.snapshotCount = snapshotCount;
        }

        public File getBackupFile() {
            return backupFile;
        }

        public int getRecordsAdded() {
            return recordsAdded;
        }

        public int getImagesAdded() {
            return imagesAdded;
        }

        public int getSnapshotCount() {
            return snapshotCount;
        }
    }

    public static final class RestoreResult {
        private final File backupFile;
        private final int recordsInserted;
        private final int recordsSkipped;
        private final int imagesRestored;
        private final int snapshotsRead;

        private RestoreResult(File backupFile, int recordsInserted, int recordsSkipped, int imagesRestored, int snapshotsRead) {
            this.backupFile = backupFile;
            this.recordsInserted = recordsInserted;
            this.recordsSkipped = recordsSkipped;
            this.imagesRestored = imagesRestored;
            this.snapshotsRead = snapshotsRead;
        }

        public File getBackupFile() {
            return backupFile;
        }

        public int getRecordsInserted() {
            return recordsInserted;
        }

        public int getRecordsSkipped() {
            return recordsSkipped;
        }

        public int getImagesRestored() {
            return imagesRestored;
        }

        public int getSnapshotsRead() {
            return snapshotsRead;
        }
    }
}
