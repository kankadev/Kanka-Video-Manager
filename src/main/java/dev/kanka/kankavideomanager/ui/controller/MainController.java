package dev.kanka.kankavideomanager.ui.controller;

import dev.kanka.kankavideomanager.enums.MEDIA_STATUS;
import dev.kanka.kankavideomanager.pojo.KnkMedia;
import dev.kanka.kankavideomanager.ui.common.FxController;
import dev.kanka.kankavideomanager.ui.custom.KnkImageView;
import dev.kanka.kankavideomanager.utils.GUIUtil;
import dev.kanka.kankavideomanager.utils.WebUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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
import java.util.Objects;
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

public class MainController extends FxController {

    private static final Logger LOGGER = LogManager.getLogger();

    private static MainController instance;

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer embeddedMediaPlayer;

    private List<Button> controlButtons;
    private List<Button> playListButtons;

    private final FontIcon pauseIcon = new FontIcon(PAUSE_CIRCLE);
    private final FontIcon playIcon = new FontIcon(PLAY_CIRCLE);

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

    @FXML
    ImageView logoImageView;

    @FXML
    Hyperlink kankaLink;

    /**
     * @return MainController singleton instance
     */
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
        initSpeedSlider();
        initControlButtons();
        initPlayList();
        initPlayListButtons();
        initDragDropListener();
        initLogo();
    }


    /**
     * setup player and its listeners
     */
    private void initPlayer() {
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        this.embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void mediaChanged(MediaPlayer mediaPlayer, MediaRef media) {
                LOGGER.debug("media: {}", media);

                timeSlider.setMin(0);
                timeSlider.setValue(0);
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                LOGGER.debug("");
                timeSlider.setMax(mediaPlayer.media().info().duration());

                Platform.runLater(() -> {
                    playPauseBtn.setText("Pause");
                    playPauseBtn.setGraphic(pauseIcon);
                });
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                LOGGER.debug("");

                Platform.runLater(() -> {
                    playPauseBtn.setText("Play");
                    playPauseBtn.setGraphic(playIcon);
                });
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                LOGGER.debug("");
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                LOGGER.debug("");

                Platform.runLater(() -> {
                    playPauseBtn.setText("Play");
                    playPauseBtn.setGraphic(playIcon);
                });
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                LOGGER.debug("newTime: {}", newTime);
                timeSlider.setValue(newTime);
            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volume) {
                LOGGER.debug("volume: {}", volume);
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

        KnkImageView videoImageView = new KnkImageView();
        ResizableImageView resizableImageView = new ResizableImageView(videoImageView);
        videoImageView.setPreserveRatio(true);
        this.embeddedMediaPlayer.videoSurface().set(videoSurfaceForImageView(videoImageView));
        resizableImageView.getStyleClass().add("knkImageView");

        borderPane.setCenter(resizableImageView);
        borderPane.getCenter().getStyleClass().add("videoFrame");
    }

    /**
     * init speed/rate GUI controls
     */
    private void initSpeedSlider() {
        speedLabel.textProperty().bind(Bindings.format("%.2fx", speedSlider.valueProperty()));
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            embeddedMediaPlayer.controls().setRate(newValue.floatValue());

            float rate = embeddedMediaPlayer.status().rate();
            LOGGER.debug("Current speed/rate: {}", rate);
        });
    }

    /**
     * init control buttons and their listeners
     */
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
        // MEDIA_STATUS column
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

        // filename column
        pathNameColumn.setCellValueFactory(new PropertyValueFactory<>("pathName"));

        // file size column
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));

        // count files in playList // TODO: not working on "empty playlist"
        playList.itemsProperty().addListener(observable -> countFilesLabel.setText(playList.getItems().size() + " files in playlist"));

        // create context menu for each row
        playList.setRowFactory(tableView -> {
            final TableRow<KnkMedia> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();
            createMenuItemsForContextMenuInPlayList(row, rowMenu);

            // only display context menu for non-null items
            row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));
            return row;
        });
    }

    /**
     * Creates menu items for the context menu for each row in the playlist.
     *
     * @param row
     * @param rowMenu
     */
    private void createMenuItemsForContextMenuInPlayList(TableRow<KnkMedia> row, ContextMenu rowMenu) {
        // copy filename to clipboard
        CustomMenuItem copyFileNameMenuItem = createCopyFileNameToClipBoardMenuItem(row);

        // copy absolute path to clipboard
        CustomMenuItem copyFullPathMenuItem = createCopyFullPathToClipBoardMenuItem(row);

        // remove media item from playlist
        CustomMenuItem removeMenuItem = createMenuItem("Removes this file from playlist.",
                "Remove from playlist", event -> removeItem(row.getItem(), false), "removeMenuItem");

        // mark media item to delete from hard drive
        CustomMenuItem deleteMenuItem = createMenuItem("Marks this file to be deleted directly from hard drive.",
                "Delete", event -> markMediaForDeletion(row.getItem(), false), "deleteMenuItem");

        // mark media item to move this file to another destination
        CustomMenuItem moveMenuItem = createMenuItem("Marks this file to be moved to another destination.",
                "Move", event -> markMediaForMoving(row.getItem(), false), "moveMenuItem");

        rowMenu.getItems().addAll(copyFileNameMenuItem, copyFullPathMenuItem, removeMenuItem, deleteMenuItem, moveMenuItem);
    }


    /**
     * Creates a menu item which copies the file name to the clipboard.
     *
     * @param row
     * @return menu item
     */
    private CustomMenuItem createCopyFileNameToClipBoardMenuItem(TableRow<KnkMedia> row) {
        Label copyLabel = GUIUtil.createLabelWithTooltip("Copy filename to clipboard", "Copy filename");
        CustomMenuItem copyMenuItem = new CustomMenuItem(copyLabel);
        copyMenuItem.getStyleClass().add("copyFilenameMenuItem");
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        copyMenuItem.setOnAction(event -> {
            content.putString(row.getItem().getName());
            clipboard.setContent(content);
        });
        return copyMenuItem;
    }

    /**
     * Creates a menu item which copies the absolute path to the clipboard.
     *
     * @param row
     * @return
     */
    private CustomMenuItem createCopyFullPathToClipBoardMenuItem(TableRow<KnkMedia> row) {
        Label copyLabel = GUIUtil.createLabelWithTooltip("Copy absolute path to clipboard", "Copy full path");
        CustomMenuItem copyMenuItem = new CustomMenuItem(copyLabel);
        copyMenuItem.getStyleClass().add("copyFullPathMenuItem");
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        copyMenuItem.setOnAction(event -> {
            content.putString(row.getItem().getAbsolutePath());
            clipboard.setContent(content);
        });
        return copyMenuItem;
    }


    private void markMediaForDeletion(KnkMedia media) {
        markMediaForDeletion(media, true);
    }

    /**
     * Sets the status of the media to be deleted.
     *
     * @param media
     * @param skip  skips to the next media file if true
     */
    private void markMediaForDeletion(KnkMedia media, boolean skip) {
        LOGGER.debug("Mark for deletion: {}", media);
        if (!isPlaylistEmpty() && media != null) {
            media.setStatus(MEDIA_STATUS.DELETE);
        }
        playList.refresh();
        if (skip) {
//            nextMedia();
        }
    }

    private void markMediaForMoving(KnkMedia media) {
        markMediaForMoving(media, true);
    }

    /**
     * Sets the status of the media to be moved to another location.
     *
     * @param media
     * @param skip  skips to the next media file in playlist if true
     */
    private void markMediaForMoving(KnkMedia media, boolean skip) {
        LOGGER.debug("Mark for moving: {}", media);
        if (!isPlaylistEmpty() && media != null) {
            media.setStatus(MEDIA_STATUS.MOVE);
        }
        playList.refresh();
        if (skip) {
//            nextMedia();
        }
    }

    /**
     * Creates a menu item.
     *
     * @param tooltipText
     * @param labelText
     * @param actionEventEventHandler
     * @param styleClass
     * @return created menu item
     */
    private CustomMenuItem createMenuItem(String tooltipText, String labelText, EventHandler<ActionEvent> actionEventEventHandler, String styleClass) {
        Label removeLabel = GUIUtil.createLabelWithTooltip(tooltipText, labelText);
        CustomMenuItem customMenuItem = new CustomMenuItem(removeLabel);
        customMenuItem.getStyleClass().add(styleClass);
        customMenuItem.setOnAction(actionEventEventHandler);
        return customMenuItem;
    }


    /**
     * Removes item from playlist and skips to the next media in playlist.
     *
     * @param media
     * @return true if item was removed from playlist successfully, otherwise false
     */
    private boolean removeItem(KnkMedia media) {
        return removeItem(media, true);
    }

    /**
     * Removes item from playlist.
     *
     * @param media
     * @param skip  skip to the next media in playlist if true
     * @return true if item was removed from playlist successfully, otherwise false
     */
    private boolean removeItem(KnkMedia media, boolean skip) {
        boolean removed = false;
        if (!isPlaylistEmpty() && media != null) {
            removed = playList.getItems().remove(media);
        }
        if (skip) {
//            next(); // TODO
        }
        return removed;
    }

    /**
     * Provides the information whether the playlist is empty or not.
     *
     * @return true if playlist is empty, otherwise false.
     */
    private boolean isPlaylistEmpty() {
        return playList.getItems().isEmpty();
    }

    /**
     * Initializes the playlist related buttons.
     */
    private void initPlayListButtons() {
        for (Button btn : playListButtons) {
            btn.disableProperty().bind(playList.getSelectionModel().selectedItemProperty().isNull());
        }

        emptyPlaylistBtn.setOnAction(event -> emptyPlaylist());
    }

    /**
     * Handles drag and drop of files.
     */
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
                    if (file.isFile()) {
                        knkMedias.add(new KnkMedia(file.getAbsolutePath()));
                    }
                    if (file.isDirectory()) {
                        for (File childFile : Objects.requireNonNull(file.listFiles())) {
                            if (childFile.isFile()) {
                                knkMedias.add(new KnkMedia(childFile.getAbsolutePath()));
                            }
                        }
                    }
                }

                knkMedias.addAll(playList.getItems());

                playList.setItems(FXCollections.observableArrayList(knkMedias));
                Collections.sort(playList.getItems());

                success = true;

                LOGGER.debug("Added file(s) to playlist: {}", knkMedias);
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

    private void initLogo() {
        logoImageView.getStyleClass().add("knkLogo");

        logoImageView.setOnMouseClicked(event -> {
            WebUtil.openKankaWebsite();
        });

        kankaLink.setOnAction(event -> {
            WebUtil.openKankaWebsite();
        });
    }
}
