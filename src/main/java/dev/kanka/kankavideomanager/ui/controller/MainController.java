package dev.kanka.kankavideomanager.ui.controller;

import dev.kanka.kankavideomanager.enums.MEDIA_STATUS;
import dev.kanka.kankavideomanager.pojo.KnkMedia;
import dev.kanka.kankavideomanager.ui.common.FxController;
import dev.kanka.kankavideomanager.utils.MediaStatusUtil;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.callback.CallbackMedia;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;

public class MainController implements FxController {

    private static final Logger logger = LogManager.getLogger();

    private static MainController instance;

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer embeddedMediaPlayer;
    private ImageView videoImageView;

    private List<Button> controlButtons;
    private List<Button> playListButtons;

    @FXML
    BorderPane borderPane;

    @FXML
    TableView<KnkMedia> playList;

    @FXML
    TableColumn<KnkMedia, String> statusColumn;

    @FXML
    TableColumn<KnkMedia, String> pathNameColumn;

    @FXML
    TableColumn<KnkMedia, String> fileSizeColumn;

    @FXML
    Button playPauseBtn, stopBtn, previousBtn, nextBtn, skipBackwardBtn, skipForwardBtn, deleteBtn, moveBtn;

    @FXML
    Button emptyPlaylistBtn, processAllFilesBtn;

    public static MainController getInstance() {
        if (MainController.instance == null) {
            MainController.instance = new MainController();
        }
        return MainController.instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        controlButtons = FXCollections.observableArrayList(playPauseBtn, stopBtn, previousBtn, nextBtn, skipBackwardBtn, skipForwardBtn, deleteBtn, moveBtn);
        playListButtons = FXCollections.observableArrayList(emptyPlaylistBtn, processAllFilesBtn);

        initPlayer();
        initControlButtons();
        initPlayList();
        initPlayListButtons();
        initDragDropListener();
    }

    private void initPlayer() {
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        this.embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                stopBtn.setDisable(false);
                playPauseBtn.setText("Pause");
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                stopBtn.setDisable(false);
                playPauseBtn.setText("Play");
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                stopBtn.setDisable(true);
                playPauseBtn.setText("Play");
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                // TODO implement timeline
            }
        });

        this.videoImageView = new ImageView();
        this.videoImageView.setPreserveRatio(true);

        videoImageView.fitWidthProperty().bind(borderPane.widthProperty());
        videoImageView.fitHeightProperty().bind(borderPane.heightProperty());

        borderPane.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            // If you need to know about resizes
        });

        borderPane.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            // If you need to know about resizes
        });

        this.videoImageView = new ImageView();
        this.videoImageView.setPreserveRatio(true);

        this.embeddedMediaPlayer.videoSurface().set(videoSurfaceForImageView(this.videoImageView));

        borderPane.setCenter(videoImageView);
    }

    private void initControlButtons() {
        for (Button btn : controlButtons) {
            btn.disableProperty().bind(playList.getSelectionModel().selectedItemProperty().isNull());
        }

        playPauseBtn.setOnAction(event -> play());
        stopBtn.setOnAction(event -> stop());
    }

    private void initPlayList() {
        // MEDIA_STATUS Column
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {

                if (status != null && !empty) {

//                    for (MEDIA_STATUS stat : MEDIA_STATUS.values()) {
//                        Label label = new Label(stat.getStatus());
//                        FontIcon icon = MediaStatusUtil.getIcon(stat);
//                        label.setGraphic(icon);
//                    }
                    final ComboBox<MEDIA_STATUS> comboBox = new ComboBox<>(FXCollections.observableArrayList(MEDIA_STATUS.values()));
                    if (comboBox.getSelectionModel().isEmpty()) {
                        comboBox.getSelectionModel().select(MEDIA_STATUS.UNPROCESSED);
                    }
                    setGraphic(comboBox);
                    setAlignment(Pos.CENTER);
                } else {
                    setGraphic(null);
                }
            }
        });


        // Filename Column
        pathNameColumn.setCellValueFactory(new PropertyValueFactory<>("pathName"));

        // File Size Column
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
    }

    private void initPlayListButtons() {
        for (Button btn : playListButtons) {
            btn.disableProperty().bind(playList.getSelectionModel().selectedItemProperty().isNull());
        }

        emptyPlaylistBtn.setOnAction(event -> emptyPlaylist());
    }

    private void initDragDropListener() {
        playList.setOnDragOver(event -> {
            if (event.getGestureSource() != playList && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });


        playList.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();

            boolean success = false;

            if (dragboard.hasFiles()) {

                List<File> newFiles = dragboard.getFiles();
                ObservableSet<KnkMedia> knkMedias = FXCollections.observableSet();

                for (File file : newFiles) {
                    knkMedias.add(new KnkMedia(file.getAbsolutePath()));
                }

                knkMedias.addAll(playList.getItems());

                playList.setItems(FXCollections.observableArrayList(knkMedias));
                Collections.sort(playList.getItems());

                success = true;

                logger.debug("Added file(s) to playlist: {}", newFiles);
            }

            /*
             * let the source know whether the file was successfully transferred and used
             */
            event.setDropCompleted(success);
            event.consume();

//            currentPlayingMediaIndex = 0;
//
//            playMedia();
//            updateInformations();
        });
    }

    private void play() {
        if(embeddedMediaPlayer != null ) {
//            embeddedMediaPlayer.controls().play();
            embeddedMediaPlayer.media().play(playList.getItems().get(0).getAbsolutePath());
        }
    }

    public void stop() {
        if (embeddedMediaPlayer != null) {
            embeddedMediaPlayer.controls().stop();
            embeddedMediaPlayer.release();
            mediaPlayerFactory.release();
        }
    }

    private void emptyPlaylist() {
        stop();
        playList.getItems().clear();
    }

}
