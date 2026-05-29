package dev.kanka.kankavideomanager.constants;

/**
 * Application-wide constants to avoid magic numbers.
 */
public final class Constants {
    
    private Constants() {
        // Utility class - prevent instantiation
    }
    
    // UI Constants
    public static final double VIDEO_BORDER_WIDTH = 6.0;
    public static final double PLAY_PAUSE_BUTTON_MIN_WIDTH = 70.0;
    public static final double SPACING = 20.0;
    public static final double PADDING = 20.0;
    
    // Window Constants
    public static final double MIN_WINDOW_WIDTH = 750.0;
    public static final double MIN_WINDOW_HEIGHT = 650.0;
    
    // Media Player Constants
    public static final int SKIP_BACKWARD_PERCENTAGE = 5; // 5% of duration
    public static final int DEFAULT_SKIP_FORWARD_PERCENTAGE = 20; // 20% of duration
    public static final int MAX_SKIP_FORWARD_PERCENTAGE = 99; // 99% of duration
    public static final int MIN_SKIP_FORWARD_PERCENTAGE = 1; // 1% of duration
    public static final int DEFAULT_VOLUME = 100; // 100%
    public static final int MAX_VOLUME = 100; // 100%
    public static final int MIN_VOLUME = 0; // 0%
    public static final int TIME_SLIDER_TICKS = 10; // Number of tick units
    
    // Media Scanner Constants
    public static final int SCANNER_THREAD_POOL_SIZE = 4;
    public static final int SCANNER_MAX_WAIT_SECONDS = 10;
    public static final int SCANNER_POLL_INTERVAL_MS = 100;
    
    // Mouse Constants
    public static final int DOUBLE_CLICK_COUNT = 2;
}
