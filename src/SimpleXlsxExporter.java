package src;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Minimal XLSX writer tailored for exporting tabular search results without external libraries.
 */
public final class SimpleXlsxExporter {
    private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private SimpleXlsxExporter() {
    }

    public enum CellType {
        STRING,
        NUMBER
    }

    public enum Orientation {
        AUTO,
        PORTRAIT,
        LANDSCAPE
    }

    public static final class ColumnSpec {
        private final String header;
        private final double width;
        private final CellType type;

        public ColumnSpec(String header, double width, CellType type) {
            this.header = Objects.requireNonNull(header, "header");
            this.width = width;
            this.type = Objects.requireNonNull(type, "type");
        }

        public String getHeader() {
            return header;
        }

        public double getWidth() {
            return width;
        }

        public CellType getType() {
            return type;
        }
    }

    public static void export(String filePath, String sheetName, List<ColumnSpec> columns, List<List<Object>> rows, Orientation orientation) throws IOException {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("At least one column specification is required");
        }
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        double totalWidth = 0.0;
        for (ColumnSpec column : columns) {
            totalWidth += column.getWidth();
        }
        Orientation requestedOrientation = orientation == null ? Orientation.AUTO : orientation;
        Orientation resolvedOrientation;
        if (requestedOrientation == Orientation.AUTO) {
            resolvedOrientation = totalWidth > 95.0 ? Orientation.LANDSCAPE : Orientation.PORTRAIT;
        } else {
            resolvedOrientation = requestedOrientation;
        }

        try (OutputStream out = Files.newOutputStream(path);
             ZipOutputStream zos = new ZipOutputStream(out)) {
            writeEntry(zos, "[Content_Types].xml", contentTypes());
            writeEntry(zos, "_rels/.rels", rootRels());
            writeEntry(zos, "docProps/core.xml", coreProperties());
            writeEntry(zos, "docProps/app.xml", appProperties());

            String sanitizedSheet = sanitizeSheetName(sheetName);
            writeEntry(zos, "xl/workbook.xml", workbookXml(sanitizedSheet));
            writeEntry(zos, "xl/_rels/workbook.xml.rels", workbookRels());
            writeEntry(zos, "xl/styles.xml", stylesXml());
            writeEntry(zos, "xl/worksheets/sheet1.xml", sheetXml(columns, rows, resolvedOrientation));
        }
    }

    private static void writeEntry(ZipOutputStream zos, String entryName, String content) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        zos.write(bytes);
        zos.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
            "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
            "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>" +
            "<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>" +
            "</Types>";
    }

    private static String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
            "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/>" +
            "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" Target=\"docProps/app.xml\"/>" +
            "</Relationships>";
    }

    private static String coreProperties() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<dc:creator>KeyBase</dc:creator>" +
            "<cp:lastModifiedBy>KeyBase</cp:lastModifiedBy>" +
            "<dcterms:created xsi:type=\"dcterms:W3CDTF\">" + ISO8601.format(now) + "</dcterms:created>" +
            "<dcterms:modified xsi:type=\"dcterms:W3CDTF\">" + ISO8601.format(now) + "</dcterms:modified>" +
            "</cp:coreProperties>";
    }

    private static String appProperties() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\">" +
            "<Application>KeyBase</Application>" +
            "</Properties>";
    }

    private static String workbookXml(String sheetName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<sheets><sheet name=\"" + escapeAttribute(sheetName) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
            "</workbook>";
    }

    private static String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
            "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
            "</Relationships>";
    }

    private static String stylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<fonts count=\"2\">" +
            "<font><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><b/><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "</fonts>" +
            "<fills count=\"2\">" +
            "<fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFDBEEF4\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "</fills>" +
            "<borders count=\"2\">" +
            "<border><left/><right/><top/><bottom/><diagonal/></border>" +
            "<border>" +
            "<left style=\"thin\"><color indexed=\"64\"/></left>" +
            "<right style=\"thin\"><color indexed=\"64\"/></right>" +
            "<top style=\"thin\"><color indexed=\"64\"/></top>" +
            "<bottom style=\"thin\"><color indexed=\"64\"/></bottom>" +
            "<diagonal/>" +
            "</border>" +
            "</borders>" +
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
            "<cellXfs count=\"3\">" +
            "<xf xfId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/>" +
            "<xf xfId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\" wrapText=\"1\"/></xf>" +
            "<xf xfId=\"0\" fontId=\"1\" fillId=\"1\" borderId=\"1\" applyBorder=\"1\" applyFill=\"1\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" +
            "</cellXfs>" +
            "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
            "</styleSheet>";
    }

    private static String sheetXml(List<ColumnSpec> columns, List<List<Object>> rows, Orientation orientation) {
        int lastRow = Math.max(1, (rows == null ? 0 : rows.size()) + 1);
        String lastCol = columnLabel(columns.size());
        String dimension = "A1:" + lastCol + lastRow;

        StringBuilder xml = new StringBuilder(4096);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">");
        xml.append("<sheetPr><pageSetUpPr fitToPage=\"1\"/></sheetPr>");
        xml.append("<dimension ref=\"").append(dimension).append("\"/>");
        xml.append("<sheetViews><sheetView workbookViewId=\"0\"><selection activeCell=\"A1\" sqref=\"A1\"/></sheetView></sheetViews>");
        xml.append("<sheetFormatPr defaultRowHeight=\"15\"/>\n");

        xml.append("<cols>");
        for (int i = 0; i < columns.size(); i++) {
            ColumnSpec column = columns.get(i);
            xml.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                .append("\" width=\"").append(String.format(Locale.US, "%.2f", column.getWidth()))
                .append("\" customWidth=\"1\"/>");
        }
        xml.append("</cols>");

        xml.append("<sheetData>");
        // Header row
        xml.append("<row r=\"1\" ht=\"18\" customHeight=\"1\">");
        for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
            ColumnSpec column = columns.get(colIndex);
            xml.append(cellXml(1, colIndex, column.getHeader(), CellType.STRING, true));
        }
        xml.append("</row>");

        if (rows != null) {
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                int excelRow = rowIndex + 2;
                xml.append("<row r=\"").append(excelRow).append("\" ht=\"16\" customHeight=\"1\">");
                List<Object> row = rows.get(rowIndex);
                for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                    Object value = (rowIndex < rows.size() && row != null && colIndex < row.size()) ? row.get(colIndex) : null;
                    ColumnSpec columnSpec = columns.get(colIndex);
                    xml.append(cellXml(excelRow, colIndex, value, columnSpec.getType(), false));
                }
                xml.append("</row>");
            }
        }

        xml.append("</sheetData>");
        xml.append("<printOptions headings=\"false\" gridLines=\"false\" gridLinesSet=\"true\" horizontalCentered=\"false\" verticalCentered=\"false\"/>");
        xml.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>");
        boolean landscape = orientation == Orientation.LANDSCAPE;
        xml.append("<pageSetup paperSize=\"9\" orientation=\"").append(landscape ? "landscape" : "portrait")
            .append("\" fitToWidth=\"1\" fitToHeight=\"1\" horizontalDpi=\"300\" verticalDpi=\"300\"/>");
        xml.append("</worksheet>");
        return xml.toString();
    }

    private static String cellXml(int rowIndex, int columnIndex, Object rawValue, CellType cellType, boolean header) {
        String cellRef = columnLabel(columnIndex + 1) + rowIndex;
        int styleIndex = header ? 2 : 1;
        if (header) {
            return "<c r=\"" + cellRef + "\" t=\"inlineStr\" s=\"" + styleIndex + "\"><is><t>" + escapeXml(rawValue == null ? "" : rawValue.toString()) + "</t></is></c>";
        }

        if (cellType == CellType.NUMBER) {
            String numeric = formatNumber(rawValue);
            if (numeric == null) {
                return "<c r=\"" + cellRef + "\" s=\"" + styleIndex + "\"/>";
            }
            return "<c r=\"" + cellRef + "\" s=\"" + styleIndex + "\"><v>" + numeric + "</v></c>";
        }

        String text = rawValue == null ? "" : rawValue.toString();
        if (text.isEmpty()) {
            return "<c r=\"" + cellRef + "\" s=\"" + styleIndex + "\"/>";
        }
        return "<c r=\"" + cellRef + "\" t=\"inlineStr\" s=\"" + styleIndex + "\"><is><t>" + escapeXml(text) + "</t></is></c>";
    }

    private static String columnLabel(int columnIndex) {
        StringBuilder label = new StringBuilder();
        int index = columnIndex;
        while (index > 0) {
            int remainder = (index - 1) % 26;
            label.insert(0, (char) ('A' + remainder));
            index = (index - 1) / 26;
        }
        return label.toString();
    }

    private static String escapeXml(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String escapeAttribute(String value) {
        return escapeXml(value).replace("'", "&apos;");
    }

    private static String sanitizeSheetName(String sheetName) {
        if (sheetName == null || sheetName.trim().isEmpty()) {
            return "Sheet1";
        }
        String sanitized = sheetName.replaceAll("[\\\\/?*\u0000-\u001F\u007F]", " ");
        sanitized = sanitized.replace('[', ' ').replace(']', ' ').replace(':', ' ');
        sanitized = sanitized.trim();
        if (sanitized.isEmpty()) {
            sanitized = "Sheet1";
        }
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }
        return sanitized;
    }

    private static String formatNumber(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        if (value instanceof Number) {
            BigDecimal decimal = new BigDecimal(value.toString());
            return decimal.toPlainString();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            BigDecimal decimal = new BigDecimal(text);
            return decimal.toPlainString();
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Orientation parseOrientation(String value) {
        if (value == null) {
            return Orientation.AUTO;
        }
        try {
            return Orientation.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return Orientation.AUTO;
        }
    }
}
