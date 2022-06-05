package dev.kanka.kankavideomanager.ui.common;

import javafx.application.HostServices;
import javafx.fxml.Initializable;

public abstract class FxController implements Initializable {

    private HostServices hostServices;

    public HostServices getHostServices() {
        return hostServices;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

}
