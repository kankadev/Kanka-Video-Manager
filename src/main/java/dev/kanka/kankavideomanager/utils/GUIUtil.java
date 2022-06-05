package dev.kanka.kankavideomanager.utils;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class GUIUtil {

    private GUIUtil() {
    }

    /**
     * creates Tooltip
     *
     * @param tooltipText
     * @param labelText
     * @return new Label with Tooltip
     */
    public static Label createLabelWithTooltip(String tooltipText, String labelText) {
        Tooltip tooltip = new Tooltip(tooltipText);
        Label label = new Label(labelText);
        Tooltip.install(label, tooltip);
        return label;
    }
}
