package src;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum ExportField {
    ID("ID", "SN", 12.0, SimpleXlsxExporter.CellType.NUMBER),
    NAME("NAME", "Name", 26.0, SimpleXlsxExporter.CellType.STRING),
    PHONE("PHONE", "Phone", 18.0, SimpleXlsxExporter.CellType.STRING),
    ID_NO("ID_NO", "ID No", 18.0, SimpleXlsxExporter.CellType.STRING),
    KEY_NO("KEY_NO", "Key No/Model", 16.0, SimpleXlsxExporter.CellType.STRING),
    CATEGORY("CATEGORY", "Category", 16.0, SimpleXlsxExporter.CellType.STRING),
    KEY_TYPE("KEY_TYPE", "Key Type", 16.0, SimpleXlsxExporter.CellType.STRING),
    PURPOSE("PURPOSE", "Purpose", 18.0, SimpleXlsxExporter.CellType.STRING),
    VEHICLE_NO("VEHICLE_NO", "Vehicle No", 18.0, SimpleXlsxExporter.CellType.STRING),
    DATE("DATE", "Date", 14.0, SimpleXlsxExporter.CellType.STRING),
    TIME("TIME", "Time", 14.0, SimpleXlsxExporter.CellType.STRING),
    REMARKS("REMARKS", "Remarks", 28.0, SimpleXlsxExporter.CellType.STRING),
    QUANTITY("QUANTITY", "Quantity", 12.0, SimpleXlsxExporter.CellType.NUMBER),
    AMOUNT("AMOUNT", "Amount", 14.0, SimpleXlsxExporter.CellType.NUMBER);

    private static final Map<String, ExportField> LOOKUP = new LinkedHashMap<>();

    static {
        for (ExportField field : values()) {
            LOOKUP.put(field.key, field);
        }
    }

    private final String key;
    private final String header;
    private final double width;
    private final SimpleXlsxExporter.CellType cellType;

    ExportField(String key, String header, double width, SimpleXlsxExporter.CellType cellType) {
        this.key = key;
        this.header = header;
        this.width = width;
        this.cellType = cellType;
    }

    public String getKey() {
        return key;
    }

    public String getHeader() {
        return header;
    }

    public double getWidth() {
        return width;
    }

    public SimpleXlsxExporter.CellType getCellType() {
        return cellType;
    }

    public static ExportField fromKey(String key) {
        if (key == null) {
            return null;
        }
        String normalized = key.trim().toUpperCase(Locale.ROOT);
        return LOOKUP.get(normalized);
    }

    public static List<ExportField> resolve(List<String> keys) {
        List<ExportField> resolved = new ArrayList<>();
        if (keys == null) {
            return resolved;
        }
        for (String key : keys) {
            ExportField field = fromKey(key);
            if (field != null && !resolved.contains(field)) {
                resolved.add(field);
            }
        }
        return resolved;
    }

    public static List<String> defaultKeys() {
        List<String> defaults = new ArrayList<>(values().length);
        for (ExportField field : values()) {
            defaults.add(field.getKey());
        }
        return defaults;
    }
}
