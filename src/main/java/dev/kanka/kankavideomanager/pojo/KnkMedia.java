package dev.kanka.kankavideomanager.pojo;

import dev.kanka.kankavideomanager.enums.MEDIA_STATUS;
import dev.kanka.kankavideomanager.utils.MediaUtils;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.File;
import java.util.Objects;

public class KnkMedia extends File {

    private final ReadOnlyStringWrapper status;
    private final ReadOnlyStringWrapper pathName;
    private final ReadOnlyStringWrapper fileSize;

    public KnkMedia(String pathname) {
        super(pathname);
        this.status = new ReadOnlyStringWrapper(MEDIA_STATUS.UNPROCESSED.toString());
        this.pathName = new ReadOnlyStringWrapper(pathname);
        this.fileSize = new ReadOnlyStringWrapper(MediaUtils.humanReadableByteCountBin(super.length()));
        // TODO use setConverter on GUI (extends StringConverter...)
    }

    public String getStatus() {
        return status.get();
    }

    public ReadOnlyStringWrapper statusProperty() {
        return status;
    }

    public void setStatus(MEDIA_STATUS status) {
        this.status.set(status.toString());
    }

    public String getPathName() {
        return pathName.get();
    }

    public ReadOnlyStringWrapper pathNameProperty() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName.set(pathName);
    }

    public String getFileSize() {
        return fileSize.get();
    }

    public ReadOnlyStringWrapper fileSizeProperty() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize.set(fileSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        KnkMedia knkMedia = (KnkMedia) o;
        return status.equals(knkMedia.status) && pathName.equals(knkMedia.pathName) && fileSize.equals(knkMedia.fileSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status, pathName, fileSize);
    }

    @Override
    public String toString() {
        return "Media{" + "status=" + status + ", pathName=" + pathName + ", fileSize=" + fileSize + '}';
    }
}
