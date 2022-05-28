package dev.kanka.kankavideomanager.utils;

import dev.kanka.kankavideomanager.enums.MEDIA_STATUS;
import org.kordamp.ikonli.javafx.FontIcon;

public class MediaStatusUtil {

    private MediaStatusUtil() {
        throw new AssertionError("Don't instantiate this class.");
    }

    public static FontIcon getIcon(MEDIA_STATUS status) {
        switch (status) {
            case DELETE:
                return new FontIcon("fas-trash");
            case MOVE:
                return new FontIcon("fas-file-export");
            default:
                return new FontIcon("fas-clock");
        }
    }
}
