package dev.kanka.kankavideomanager.utils;

/**
 * OS Utils
 */
public class OSUtils {

    private OSUtils() {
    }

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    }

    private static OS os = null;

    public static OS getOS() {
        if (os == null) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux")
                    || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            }
        }
        return os;
    }

    public static boolean isWindows() {
        return getOS() == OS.WINDOWS;
    }

    public static boolean isLinux() {
        return getOS() == OS.LINUX;
    }
}
