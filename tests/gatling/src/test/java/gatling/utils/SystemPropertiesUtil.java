package gatling.utils;

public final class SystemPropertiesUtil {

    public static String getAsStringOrElse(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null) {
            return fallback;
        }
        return value;
    }

    public static double getAsDoubleOrElse(String key, double fallback) {
        String value = System.getProperty(key);
        if (value == null) {
            return fallback;
        }
        return Double.parseDouble(value);
    }

    public static int getAsIntOrElse(String key, int fallback) {
        String value = System.getProperty(key);
        if (value == null) {
            return fallback;
        }
        return Integer.parseInt(value);
    }

    public static long getAsLongOrElse(String key, long fallback) {
        String value = System.getProperty(key);
        if (value == null) {
            return fallback;
        }
        return Long.parseLong(value);
    }

    public static boolean getAsBooleanOrElse(String key, boolean fallback) {
        String value = System.getProperty(key);
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(value);
    }
}
