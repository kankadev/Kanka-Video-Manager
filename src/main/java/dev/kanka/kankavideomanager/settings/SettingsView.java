package dev.kanka.kankavideomanager.settings;

import com.dlsc.preferencesfx.PreferencesFx;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SettingsView extends VBox {
    private PreferencesFx preferencesFx;
    private MenuBar menuBar;
    private Menu menu;
    private MenuItem preferencesMenuItem;
    private SettingsController settingsController;

    private Label welcomeLbl;
    private Label brightnessLbl;
    private Label nightModeLbl;
    private Label scalingLbl;
    private Label screenNameLbl;
    private Label resolutionLbl;
    private Label orientationLbl;
    private Label favoritesLbl;
    private Label fontSizeLbl;
    private Label lineSpacingLbl;
    private Label favoriteNumberLbl;

    // VBox with descriptions
    private CheckBox instantPersistence = new CheckBox("Instant Persistence");

    public SettingsView(PreferencesFx preferencesFx, SettingsController settingsController) {
        this.preferencesFx = preferencesFx;
        this.settingsController = settingsController;

        initializeParts();
        layoutParts();
        setupBindings();
        setupEventHandlers();
        setupListeners();
    }

    private void initializeParts() {
        menuBar = new MenuBar();
        menu = new Menu("Edit");
        preferencesMenuItem = new MenuItem("Preferences");

        welcomeLbl = new Label();
        brightnessLbl = new Label();
        nightModeLbl = new Label();
        scalingLbl = new Label();
        screenNameLbl = new Label();
        resolutionLbl = new Label();
        orientationLbl = new Label();
        favoritesLbl = new Label();
        fontSizeLbl = new Label();
        lineSpacingLbl = new Label();
        favoriteNumberLbl = new Label();
    }

    private void layoutParts() {
        // MenuBar
        menu.getItems().add(preferencesMenuItem);
        menuBar.getMenus().add(menu);

        // VBox with values
        VBox valueBox = new VBox(
                welcomeLbl,
                brightnessLbl,
                nightModeLbl,
                scalingLbl,
                screenNameLbl,
                resolutionLbl,
                orientationLbl,
                favoritesLbl,
                fontSizeLbl,
                lineSpacingLbl,
                favoriteNumberLbl,
                instantPersistence);
        instantPersistence.setSelected(true);
        valueBox.setSpacing(20);
        valueBox.setPadding(new Insets(20, 0, 0, 20));

        VBox descriptionBox = new VBox(
                new Label("Welcome Text:"),
                new Label("Brightness:"),
                new Label("Night mode:"),
                new Label("Scaling:"),
                new Label("Screen name:"),
                new Label("Resolution:"),
                new Label("Orientation:"),
                new Label("Favorites:"),
                new Label("Font Size:"),
                new Label("Line Spacing:"),
                new Label("Favorite Number:"),
                new Label("Instant Persistence:"));
        descriptionBox.setSpacing(20);
        descriptionBox.setPadding(new Insets(20, 0, 0, 20));

        // Put everything together
        getChildren().addAll(
                menuBar,
                new HBox(
                        descriptionBox,
                        valueBox));

        // Styling
        getStyleClass().add("demo-view");
    }

    private void setupBindings() {
    }

    private void setupEventHandlers() {
        preferencesMenuItem.setOnAction(e -> preferencesFx.show(true));
    }

    private void setupListeners() {
        instantPersistence.selectedProperty().addListener((observable, oldPersistence, newPersistence) -> {
            preferencesFx.instantPersistent(newPersistence);
        });
    }

}