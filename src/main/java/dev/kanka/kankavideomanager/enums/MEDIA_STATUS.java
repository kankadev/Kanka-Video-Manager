package dev.kanka.kankavideomanager.enums;

import dev.kanka.kankavideomanager.pojo.KnkMedia;

/**
 * represents the status of a {@link KnkMedia} file.
 * <p>
 * DELETE = file will be deleted directly from hard disk
 * MOVE = file will be moved to another directory
 * UNPROCESSED = do nothing with file
 */
public enum MEDIA_STATUS {
    UNPROCESSED("UNPROCESSED"), DELETE("DELETE"), MOVE("MOVE");

    private final String status;

    MEDIA_STATUS(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}