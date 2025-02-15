package dev.kanka.kankavideomanager.utils;

import dev.kanka.kankavideomanager.ui.controller.MainController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stuff to handle web stuff like opening links in the browser
 */
public class WebUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    private WebUtil() {

    }

    public static void openWebsite(String url) {
        LOGGER.debug("Opening website: {}", url);
        if (url != null && !url.isEmpty()) {
            MainController.getInstance().getHostServices().showDocument(url);
        }
    }

    public static void openKankaWebsite() {
        LOGGER.debug("Opening Kanka website");
        openWebsite("https://kanka.dev?src=KankaVideoManager");
    }
}
