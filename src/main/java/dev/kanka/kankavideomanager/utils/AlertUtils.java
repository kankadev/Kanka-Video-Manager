package dev.kanka.kankavideomanager.utils;

import javafx.scene.control.Alert;

public class AlertUtils {

    private AlertUtils() {

    }

    private static Alert build(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        return alert;
    }

    public static Alert confirm(String title, String headerText, String contentText) {
        return build(Alert.AlertType.CONFIRMATION, title, headerText, contentText);
    }
}
