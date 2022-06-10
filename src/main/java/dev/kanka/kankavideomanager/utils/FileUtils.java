package dev.kanka.kankavideomanager.utils;

/**
 * File Utils like get human readable filesize
 */
public class FileUtils {

    private FileUtils() {

    }

    /**
     * @param bytes
     * @param si
     * @return human readable byte count
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
