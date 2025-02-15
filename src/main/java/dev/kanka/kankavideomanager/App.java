package dev.kanka.kankavideomanager;

import dev.kanka.kankavideomanager.ui.common.FxmlFile;
import dev.kanka.kankavideomanager.ui.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public final void start(Stage primaryStage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FxmlFile.MAIN_WINDOW.toString()));
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        fxmlLoader.setLocation(getClass().getResource(FxmlFile.MAIN_WINDOW.toString()));
        Parent pane = fxmlLoader.load();
        Scene scene = new Scene(pane);
        scene.getStylesheets().add(getClass().getResource("/dev/kanka/kankavideomanager/css/main.css").toExternalForm());
        MainController.getInstance().setHostServices(getHostServices());

        primaryStage.setTitle("Kanka Video Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(650);
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(event -> {
            LOGGER.debug("close request");
            Platform.exit();
        });
        primaryStage.getIcons()
                .add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/dev/kanka/kankavideomanager/icons/icon.png"))));
        primaryStage.show();
    }

    @Override
    public final void stop() {
        LOGGER.debug("close()");
        MainController.getInstance().stopAndRelease();
    }

    public static void main(String[] args) {
        launch(args);
    }

}