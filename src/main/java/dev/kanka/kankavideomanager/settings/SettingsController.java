package dev.kanka.kankavideomanager.settings;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.IntegerField;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.formsfx.view.controls.IntegerSliderControl;
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

    // Play settings
    IntegerProperty skipForwardByProperty = new SimpleIntegerProperty(20);
    IntegerField skipForwardByControl = Field.ofIntegerType(skipForwardByProperty).render(new IntegerSliderControl(1, 99));


    IntegerProperty volumeProperty = new SimpleIntegerProperty(100);
    IntegerField volumeControl = Field.ofIntegerType(volumeProperty).render(
            new IntegerSliderControl(0, 100));


    // Move settings
    ObjectProperty<File> moveDestinationProperty = new SimpleObjectProperty<>();

    public SettingsController() {
        preferencesFx = createPreferences();
        //getChildren().add(new SettingsView(preferencesFx, this));
    }

    private PreferencesFx createPreferences() {
        return PreferencesFx.of(SettingsController.class,
                Category.of("Play",
                        Group.of("Volume",
                                Setting.of("Default volume at program start", volumeControl, volumeProperty)
                        ),
                        Group.of("Skip",
                                Setting.of("Skip forward by <value> percent", skipForwardByControl, skipForwardByProperty)
                        )),

                Category.of("Move",
                        Group.of("Move",
                                Setting.of("Where to move the files", moveDestinationProperty, "Choose directory", null, true)
                        )
                )
        ).persistWindowState(false).saveSettings(true).debugHistoryMode(false).buttonsVisibility(true);
    }


    public int getSkipForwardBy() {
        return skipForwardByProperty.get();
    }

    public IntegerProperty skipForwardByProperty() {
        return skipForwardByProperty;
    }

    public int getVolume() {
        return volumeProperty.get();
    }

    public IntegerProperty volumeProperty() {
        return volumeProperty;
    }

    public File getMoveDestination() {
        return moveDestinationProperty.get();
    }

    public ObjectProperty<File> moveDestinationProperty() {
        return moveDestinationProperty;
    }
}