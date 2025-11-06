package src;

import java.util.Locale;

/**
 * Utility helpers for deriving, stripping, and formatting service type metadata
 * from remarks text. Service type is currently stored implicitly via well-known
 * remark suffixes, so this helper makes the behaviour consistent across UI
 * components.
 */
public final class ServiceTypeHelper {
    private ServiceTypeHelper() {}

    public enum ServiceType {
        DUPLICATE("Duplicate"),
        IN_SHOP("In-shop"),
        ON_SITE("On-site");

        private final String displayName;

        ServiceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static final String IN_SHOP_SUFFIX = "Key Lost (Key made in shop)";
    public static final String ON_SITE_SUFFIX = "Key Lost (Key done on site)";
    public static final String IN_SHOP_KEYWORD = "key made in shop";
    public static final String ON_SITE_KEYWORD = "key done on site";

    public static ServiceType detectServiceType(String remarks) {
        if (remarks == null || remarks.trim().isEmpty()) {
            return ServiceType.DUPLICATE;
        }
        String lowerRemarks = remarks.toLowerCase(Locale.ROOT);
        if (lowerRemarks.contains(IN_SHOP_KEYWORD)) {
            return ServiceType.IN_SHOP;
        }
        if (lowerRemarks.contains(ON_SITE_KEYWORD)) {
            return ServiceType.ON_SITE;
        }
        return ServiceType.DUPLICATE;
    }

    public static String stripServiceSuffix(String remarks) {
        if (remarks == null || remarks.trim().isEmpty()) {
            return "";
        }
        ServiceType serviceType = detectServiceType(remarks);
        if (serviceType == ServiceType.DUPLICATE) {
            return remarks.trim();
        }

        String suffix = serviceType == ServiceType.IN_SHOP ? IN_SHOP_SUFFIX : ON_SITE_SUFFIX;
        String trimmed = remarks.trim();
        String lowerTrimmed = trimmed.toLowerCase(Locale.ROOT);
        String lowerSuffix = suffix.toLowerCase(Locale.ROOT);
        int suffixIndex = lowerTrimmed.lastIndexOf(lowerSuffix);
        if (suffixIndex >= 0) {
            String beforeSuffix = trimmed.substring(0, suffixIndex).trim();
            while (beforeSuffix.endsWith("-")) {
                beforeSuffix = beforeSuffix.substring(0, beforeSuffix.length() - 1).trim();
            }
            return beforeSuffix;
        }
        return trimmed;
    }

    public static String applyServiceType(String baseRemarks, ServiceType serviceType) {
        String remarks = baseRemarks == null ? "" : baseRemarks.trim();
        if (serviceType == null || serviceType == ServiceType.DUPLICATE) {
            return remarks;
        }
        String suffix = serviceType == ServiceType.IN_SHOP ? IN_SHOP_SUFFIX : ON_SITE_SUFFIX;
        if (remarks.isEmpty()) {
            return suffix;
        }
        return remarks + " - " + suffix;
    }
}
