package dev.kanka.kankavideomanager;

import dev.kanka.kankavideomanager.ui.common.FxmlFile;
import dev.kanka.kankavideomanager.ui.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


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

//        MainController controller = fxmlLoader.getController();
        MainController.getInstance().setHostServices(getHostServices());
//        controller.setHostServices(getHostServices());

        primaryStage.setTitle("Kanka Video Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(650);
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(event -> Platform.exit());
        primaryStage.show();
    }

    @Override
    public final void stop() {
        MainController.getInstance().stopAndRelease();
    }

    public static void main(String[] args) {
        launch(args);
    }


}