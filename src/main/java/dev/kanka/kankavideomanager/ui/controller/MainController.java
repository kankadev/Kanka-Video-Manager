package dev.kanka.kankavideomanager.ui.controller;

import dev.kanka.kankavideomanager.enums.MEDIA_STATUS;
import dev.kanka.kankavideomanager.pojo.KnkMedia;
import dev.kanka.kankavideomanager.ui.common.FxController;
import dev.kanka.kankavideomanager.ui.custom.KnkImageView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.view.ResizableImageView;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static org.kordamp.ikonli.fontawesome5.FontAwesomeRegular.PAUSE_CIRCLE;
import static org.kordamp.ikonli.fontawesome5.FontAwesomeRegular.PLAY_CIRCLE;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignD.DELETE;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignF.FILE_MOVE;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignP.PLAYLIST_CHECK;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignP.PLAYLIST_REMOVE;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignS.*;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignV.VOLUME_HIGH;
import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;

public class MainController implements FxController {

    private static final Logger LOGGER = LogManager.getLogger();

    private static MainController instance;

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer embeddedMediaPlayer;
    private KnkImageView videoImageView;

    private List<Button> controlButtons;
    private List<Button> playListButtons;

    private static KnkMedia currentPlayingMedia;

    @FXML
    BorderPane borderPane;

    @FXML
    Slider timeSlider, volumeSlider, speedSlider;

    @FXML
    Button playPauseBtn, stopBtn, previousBtn, nextBtn, skipBackwardBtn, skipForwardBtn, deleteBtn, moveBtn;

    @FXML
    TableView<KnkMedia> playList;

    @FXML
    TableColumn<KnkMedia, String> statusColumn;

    @FXML
    TableColumn<KnkMedia, String> pathNameColumn;

    @FXML
    TableColumn<KnkMedia, String> fileSizeColumn;

    @FXML
    Label countFilesLabel, volumeIcon, volumeLabel, speedLabel;

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
        initTimeSlider();
        initSpeedSlider();
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
            public void mediaChanged(MediaPlayer mediaPlayer, MediaRef media) {
                LOGGER.debug("mediaPlayer: {}, media: {}", mediaPlayer, media);
                timeSlider.setMin(0);
                timeSlider.setValue(0);
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                LOGGER.debug("mediaPlayer: {}", mediaPlayer);
                timeSlider.setMax(mediaPlayer.media().info().duration());

                Platform.runLater(() -> {
                    playPauseBtn.setText("Pause");
                    playPauseBtn.setGraphic(new FontIcon(PAUSE_CIRCLE));
                });
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                LOGGER.debug("mediaPlayer: {}", mediaPlayer);
                Platform.runLater(() -> {
                    playPauseBtn.setText("Play");
                    playPauseBtn.setGraphic(new FontIcon(PLAY_CIRCLE));
                });
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                LOGGER.debug("mediaPlayer: {}", mediaPlayer);
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                LOGGER.debug("mediaPlayer: {}", mediaPlayer);

                Platform.runLater(() -> {
                    playPauseBtn.setText("Play");
                    playPauseBtn.setGraphic(new FontIcon(PLAY_CIRCLE));
                });
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                LOGGER.debug("newTime: {}", newTime);
                timeSlider.setValue(newTime);
            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volume) {
                LOGGER.debug("mediaPlayer: {}, volume: {}", mediaPlayer, volume);
            }
        });

        borderPane.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            // If you need to know about resizes
            LOGGER.debug("borderPane's width: {}", newValue);
        });

        borderPane.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            // If you need to know about resizes
            LOGGER.debug("borderPane's height: {}", newValue);
        });

        this.videoImageView = new KnkImageView();
        ResizableImageView resizableImageView = new ResizableImageView(videoImageView);
        this.videoImageView.setPreserveRatio(true);
        this.embeddedMediaPlayer.videoSurface().set(videoSurfaceForImageView(this.videoImageView));


        resizableImageView.getStyleClass().add("knkImageView");

        borderPane.setCenter(resizableImageView);
        borderPane.getCenter().getStyleClass().setAll("videoFrame");
    }

    private void initTimeSlider() {

    }

    private void initSpeedSlider() {

        speedLabel.textProperty().bind(Bindings.format("%.2fx", speedSlider.valueProperty()));
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            embeddedMediaPlayer.controls().setRate(newValue.floatValue());

            float rate = embeddedMediaPlayer.status().rate();
            LOGGER.debug("Current speed/rate: {}", rate);
        });
    }

    private void initControlButtons() {
        for (Button btn : controlButtons) {
            btn.disableProperty().bind(playList.getSelectionModel().selectedItemProperty().isNull());
        }

        Platform.runLater(() -> {
            playPauseBtn.setText("Play");
            playPauseBtn.setGraphic(new FontIcon(PLAY_CIRCLE));
            playPauseBtn.setOnAction(event -> play());

            stopBtn.setOnAction(event -> stop());
            stopBtn.setGraphic(new FontIcon(STOP_CIRCLE));

            previousBtn.setGraphic(new FontIcon(SKIP_PREVIOUS));
            nextBtn.setGraphic(new FontIcon(SKIP_NEXT));

            skipBackwardBtn.setGraphic(new FontIcon(SKIP_BACKWARD));
            skipBackwardBtn.setOnAction(event -> {
                if (embeddedMediaPlayer != null && embeddedMediaPlayer.media().info() != null) {
                    embeddedMediaPlayer.controls().skipTime(embeddedMediaPlayer.media().info().duration() / 20 * -1);
                }
            });
            skipForwardBtn.setGraphic(new FontIcon(SKIP_FORWARD));
            skipForwardBtn.setOnAction(event -> {
                if (embeddedMediaPlayer != null && embeddedMediaPlayer.media().info() != null) {
                    embeddedMediaPlayer.controls().skipTime((embeddedMediaPlayer.media().info().duration() / 20));
                }
            });

            deleteBtn.setGraphic(new FontIcon(DELETE));
            moveBtn.setGraphic(new FontIcon(FILE_MOVE));

            emptyPlaylistBtn.setGraphic(new FontIcon(PLAYLIST_REMOVE));
            processAllFilesBtn.setGraphic(new FontIcon(PLAYLIST_CHECK));

            volumeIcon.setGraphic(new FontIcon(VOLUME_HIGH));
            volumeIcon.setText(null);
            volumeLabel.textProperty().bind(Bindings.format("%.0f", volumeSlider.valueProperty()));
            embeddedMediaPlayer.audio().setVolume((int) volumeSlider.getValue() / 100);
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> embeddedMediaPlayer.audio().setVolume(newValue.intValue()));
        });
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

                    comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        LOGGER.debug("comboBox selection changed from {} to {}", oldValue, newValue);
                    });

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

        // Count files in playList // TODO: not working on "empty playlist"
        playList.itemsProperty().addListener(observable -> countFilesLabel.setText(playList.getItems().size() + " files in playlist"));
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

                LOGGER.debug("Added file(s) to playlist: {}", newFiles);
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
        if (embeddedMediaPlayer != null) {
//            embeddedMediaPlayer.controls().play();
            embeddedMediaPlayer.media().play(playList.getItems().get(0).getAbsolutePath());
        }
    }

    private void stop() {
        if (embeddedMediaPlayer != null) {
            embeddedMediaPlayer.controls().stop();
        }
    }

    private void release() {
        if (embeddedMediaPlayer != null) {
            embeddedMediaPlayer.release();
        }
        if (mediaPlayerFactory != null) {
            mediaPlayerFactory.release();
        }
    }

    public void stopAndRelease() {
        stop();
        release();
    }

    private void emptyPlaylist() {
        stop();
        playList.getItems().clear();
        countFilesLabel.setText("0 files in playlist");
    }

}
