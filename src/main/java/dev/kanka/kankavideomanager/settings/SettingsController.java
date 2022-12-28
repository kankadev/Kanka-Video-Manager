package dev.kanka.kankavideomanager.settings;

import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;

import java.io.File;

public class SettingsController extends StackPane {

    public PreferencesFx preferencesFx;

    // General settings

    // Play settings
    IntegerProperty skipForwardBy = new SimpleIntegerProperty(20);
    IntegerProperty volumeProperty = new SimpleIntegerProperty(100);

    // Move settings
    ObjectProperty<File> moveDestination = new SimpleObjectProperty<>();

    public SettingsController() {
        preferencesFx = createPreferences();
        //getChildren().add(new SettingsView(preferencesFx, this));
    }

    private PreferencesFx createPreferences() {
        return PreferencesFx.of(SettingsController.class,
                Category.of("General"
                ),
                Category.of("Play",
                        Group.of("Volume",
                                Setting.of("Default volume at program start", volumeProperty, 0, 100)
                                        .validate(IntegerRangeValidator.between(0, 100, "Must be between 0 and 100"))
                        ),
                        Group.of("Skip",
                                Setting.of("Skip forward by <value> percent", skipForwardBy, 1, 99)
                                        .validate(IntegerRangeValidator
                                                .between(1, 99, "Must be between 1 and 99")
                                        )
                        )),

                Category.of("Move",
                        Group.of("Move",
                                Setting.of("Where to move the files", moveDestination, "Choose directory", null, true)
                        )
                )
        ).persistWindowState(false).saveSettings(true).debugHistoryMode(false).buttonsVisibility(true);
    }


    public int getSkipForwardBy() {
        return skipForwardBy.get();
    }

    public IntegerProperty skipForwardByProperty() {
        return skipForwardBy;
    }

    public int getVolumeProperty() {
        return volumeProperty.get();
    }

    public IntegerProperty volumePropertyProperty() {
        return volumeProperty;
    }

    public File getMoveDestination() {
        return moveDestination.get();
    }

    public ObjectProperty<File> moveDestinationProperty() {
        return moveDestination;
    }
}