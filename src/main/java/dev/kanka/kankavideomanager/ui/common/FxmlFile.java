package dev.kanka.kankavideomanager.ui.common;

public enum FxmlFile {
    MAIN_WINDOW("/dev/kanka/kankavideomanager/fxml/main_window.fxml");

    private final String resourcePathString;

    FxmlFile(String resourcePathString) {
        this.resourcePathString = resourcePathString;
    }

    @Override
    public String toString() {
        return resourcePathString;
    }
}
