package dev.kanka.kankavideomanager.ui.controller;

import dev.kanka.kankavideomanager.enums.MEDIA_STATUS;
import dev.kanka.kankavideomanager.pojo.KnkMedia;
import dev.kanka.kankavideomanager.settings.SettingsController;
import dev.kanka.kankavideomanager.ui.common.FxController;
import dev.kanka.kankavideomanager.ui.custom.KnkImageView;
import dev.kanka.kankavideomanager.utils.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.javafx.view.ResizableImageView;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.kordamp.ikonli.fontawesome5.FontAwesomeRegular.PAUSE_CIRCLE;
import static org.kordamp.ikonli.fontawesome5.FontAwesomeRegular.PLAY_CIRCLE;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignD.DELETE;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignF.FILE_MOVE;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignP.PLAYLIST_CHECK;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignP.PLAYLIST_REMOVE;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignS.*;
import static org.kordamp.ikonli.materialdesign2.MaterialDesignV.VOLUME_HIGH;

public class MainController extends FxController {

    private static final Logger LOGGER = LogManager.getLogger();

    private static MainController instance;

    // declare player stuff
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer embeddedMediaPlayer;
    private static KnkMedia currentPlayingMedia;
    private final SimpleIntegerProperty currentPlayingIndex = new SimpleIntegerProperty(); // TODO use this property to determine if is current media the first or last in the list and disable previous or next buttons
    private final List<KnkMedia> toBeDeletedList = new ArrayList<>();
    private final List<KnkMedia> toBeMovedList = new ArrayList<>();

    // declare controls stuff
    private List<Button> controlButtons;
    private List<Button> playListButtons;
    private final FontIcon pauseIcon = new FontIcon(PAUSE_CIRCLE);
    private final FontIcon playIcon = new FontIcon(PLAY_CIRCLE);

    private final SimpleIntegerProperty deletedFilesCount = new SimpleIntegerProperty();
    private final SimpleLongProperty deletedFilesSize = new SimpleLongProperty();
    private final SimpleIntegerProperty movedFilesCount = new SimpleIntegerProperty();

    private final SettingsController settingsController = new SettingsController();

    @FXML
    BorderPane borderPane;

    @FXML
    MenuItem settingsMenuItem, aboutMenuItem, closeMenuItem;

    @FXML
    Slider timeSlider, volumeSlider, speedSlider;

    @FXML
    Button playPauseBtn, stopBtn, previousBtn, nextBtn, skipBackwardBtn, skipForwardBtn, deleteBtn, moveBtn;

    @FXML
    TableView<KnkMedia> playList;

    @FXML
    TableColumn<KnkMedia, String> statusColumn, pathNameColumn, fileSizeColumn, commentColumn;

    @FXML
    TableColumn<KnkMedia, Long> durationColumn;

    @FXML
    Label countFilesLabel, volumeIcon, volumeLabel, speedLabel, deletedFilesLabel, movedFilesLabel;

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
        initStatistics();
        initLogo();

        settingsMenuItem.setOnAction(event -> settingsController.preferencesFx.show(true));
        closeMenuItem.setOnAction(event -> Platform.exit());
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
                long duration = mediaPlayer.media().info().duration();
                timeSlider.setMax(duration);
                timeSlider.setMajorTickUnit((int) duration / 25.0);

                Platform.runLater(() -> {
                    playPauseBtn.setText("Pause");
                    playPauseBtn.setGraphic(pauseIcon);
                    // TODO: find a better solution to get the duration... not here in playing... immediately after adding to the playlist
                    currentPlayingMedia.setDuration(duration);
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
                //LOGGER.debug("newTime: {}", newTime);
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
        this.embeddedMediaPlayer.videoSurface().set(new ImageViewVideoSurface(videoImageView));
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
        speedSlider.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getClickCount() == 2) {
                    speedSlider.setValue(1);
                }
            }
        });
    }

    /**
     * init control buttons and their listeners
     */
    private void initControlButtons() {
        for (Button btn : controlButtons) {
            btn.setDisable(true);
        }

        Platform.runLater(() -> {
            // Play
            playPauseBtn.setText("Play");
            playPauseBtn.setGraphic(new FontIcon(PLAY_CIRCLE));
            playPauseBtn.setOnAction(event -> play());

            // Stop
            stopBtn.setOnAction(event -> stop());
            stopBtn.setGraphic(new FontIcon(STOP_CIRCLE));

            // Previous
            previousBtn.setGraphic(new FontIcon(SKIP_PREVIOUS));
            previousBtn.setOnAction(event -> previous());

            // Next
            nextBtn.setGraphic(new FontIcon(SKIP_NEXT));
            nextBtn.setOnAction(event -> next());

            // Skip buttons
            skipBackwardBtn.setGraphic(new FontIcon(SKIP_BACKWARD));
            skipBackwardBtn.setOnAction(event -> {
                if (embeddedMediaPlayer != null && embeddedMediaPlayer.media().info() != null) {
                    embeddedMediaPlayer.controls().skipTime(embeddedMediaPlayer.media().info().duration() / 20 * -1);
                }
            });
            skipForwardBtn.setGraphic(new FontIcon(SKIP_FORWARD));
            skipForwardBtn.setOnAction(event -> {
                if (embeddedMediaPlayer != null && embeddedMediaPlayer.media().info() != null) {

                    long duration = embeddedMediaPlayer.media().info().duration();
                    int skipForwardBy = settingsController.getSkipForwardBy();
                    long delta = (long) (duration * (skipForwardBy / 100.0));
                    embeddedMediaPlayer.controls().skipTime(delta);
                }
            });

            // Delete Button
            deleteBtn.setGraphic(new FontIcon(DELETE));
            deleteBtn.setOnAction(event -> markMediaForDeletion(currentPlayingMedia));

            // Move button
            moveBtn.setGraphic(new FontIcon(FILE_MOVE));
            moveBtn.setOnAction(event -> markMediaForMoving(currentPlayingMedia));

            // Empty Playlist
            emptyPlaylistBtn.setGraphic(new FontIcon(PLAYLIST_REMOVE));

            // Process all files
            processAllFilesBtn.setGraphic(new FontIcon(PLAYLIST_CHECK));
            processAllFilesBtn.setOnAction(event -> processAllFiles());

            // Volume
            volumeIcon.setGraphic(new FontIcon(VOLUME_HIGH));
            volumeIcon.setText(null);
            volumeLabel.textProperty().bind(Bindings.format("%.0f", volumeSlider.valueProperty()));
            embeddedMediaPlayer.audio().setVolume((int) volumeSlider.getValue() / 100);
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> embeddedMediaPlayer.audio().setVolume(newValue.intValue()));
            volumeSlider.setValue(settingsController.getVolume());
            volumeSlider.setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    if (event.getClickCount() == 2) {
                        volumeSlider.setValue(settingsController.getVolume());
                    }
                }
            });

            // Timeline Slider
            // TODO: not working properly
            timeSlider.setOnMouseClicked(event -> {
                if (embeddedMediaPlayer.status().isPlaying()) {
                    float pos = (float) (timeSlider.getValue() / timeSlider.getMax());
                    LOGGER.debug("max: {}", timeSlider.getMax());
                    LOGGER.debug("current: {}", timeSlider.getValue());
                    LOGGER.debug("desired pos: {}", pos);
                    embeddedMediaPlayer.controls().setPosition(pos);
                }
            });
        });
    }

    private void initPlayList() {
        // MEDIA_STATUS column
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {

                if (status != null && !empty) {
                    final ComboBox<MEDIA_STATUS> comboBox = new ComboBox<>(FXCollections.observableArrayList(MEDIA_STATUS.values()));
                    comboBox.getSelectionModel().select(MEDIA_STATUS.valueOf(status));
                    comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        LOGGER.debug("comboBox selection changed from {} to {}", oldValue, newValue);
                        getTableRow().getItem().setStatus(newValue);
                    });

                    setGraphic(comboBox);
                    setAlignment(Pos.CENTER);
                } else {
                    setGraphic(null);
                    setText(null);
                }
            }
        });

        // filename column
        pathNameColumn.setCellValueFactory(new PropertyValueFactory<>("pathName"));

        // file size column
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));

        // duration column
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long duration, boolean empty) {
                if (duration != null && !empty) {
                    String durationString = String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(duration),
                            TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                    );
                    setText(durationString);
                } else {
                    setGraphic(null);
                    setText(null);
                }
            }
        });

        // comment column
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String comment, boolean empty) {
                LOGGER.debug("comment: {}, empty: {}", comment, empty);

                // TODO ..............

                if (!empty) {
                    final TextField textField = new TextField(comment);
                    textField.textProperty().addListener((observable, oldValue, newValue) -> getTableRow().getItem().setComment(newValue));

                    setGraphic(textField);
                    setAlignment(Pos.CENTER);

                } else {
                    setGraphic(null);
                    setText(null);
                }

            }
        });

        playList.itemsProperty().addListener((observable, oldItems, newItems) -> {
            countFilesLabel.textProperty().bind(
                    Bindings.size(newItems).asString("%s items in playlist"));

            for (Button btn : playListButtons) {
                btn.disableProperty().bind(Bindings.isEmpty(newItems));
            }

            for (Button btn : controlButtons) {
                if (btn == nextBtn) {
                    // TODO
                    btn.disableProperty().bind(Bindings.isEmpty(newItems).or(Bindings.equal(currentPlayingIndexProperty(), playList.getItems().size() - 1)));
                } else if (btn == previousBtn) {
                    btn.disableProperty().bind(Bindings.isEmpty(newItems).or(Bindings.equal(currentPlayingIndex, 0)));
                } else {
                    btn.disableProperty().bind(Bindings.isEmpty(newItems));
                }
            }

        });

        // create context menu for each row
        playList.setRowFactory(tableView -> {
            final TableRow<KnkMedia> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();
            createMenuItemsForContextMenuInPlayList(row, rowMenu);

            // only display context menu for non-null items
            row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));

            // play on double click
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    KnkMedia knkMedia = row.getItem();
                    play(knkMedia);
                }
            });

            return row;
        });
    }

    /**
     * Initializes the playlist related buttons.
     */
    private void initPlayListButtons() {
        for (Button btn : playListButtons) {
            btn.setDisable(true);
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

            play();
        });
    }

    private void initStatistics() {
        deletedFilesLabel.textProperty().bind(Bindings.createStringBinding(
                () -> String.format(
                        "Deleted files: %d (%s)",
                        deletedFilesCountProperty().get(),
                        FileUtils.humanReadableByteCount(deletedFilesSizeProperty().get(), true)
                ),
                deletedFilesCountProperty(),
                deletedFilesSizeProperty()
        ));
    }

    private void initLogo() {
        logoImageView.getStyleClass().add("knkLogo");
        logoImageView.setOnMouseClicked(event -> WebUtil.openKankaWebsite());
        kankaLink.setOnAction(event -> WebUtil.openKankaWebsite());
    }

    /**
     * Handles all files in playlist depending on their status: delete or move
     */
    private void processAllFiles() {
        refreshMediaStatus();
        stop();

        LOGGER.debug("In toBeDeletedList: " + toBeDeletedList);
        LOGGER.debug("In toBeMovedList: " + toBeMovedList);

        if (toBeDeletedList.size() > 0 || toBeMovedList.size() > 0) {
            Alert alert = AlertUtils.confirm("Process all files", "Are you sure to continue?",
                    toBeDeletedList.size() + " files will be deleted.\n" + toBeMovedList.size() + " files will be moved.");
            Optional<ButtonType> buttonType = alert.showAndWait();

            if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
                for (KnkMedia media : toBeDeletedList) {
                    long length = media.length();
                    if (media.delete()) {
                        deletedFilesCount.set(deletedFilesCount.get() + 1);
                        deletedFilesSize.set(deletedFilesSize.get() + length);
                        Platform.runLater(() -> playList.getItems().remove(media));
                        LOGGER.debug("Deleted: {}", media);
                    } else {
                        LOGGER.error("Couldn't be deleted: {}", media);
                    }
                }

                for (KnkMedia media : toBeMovedList) {
                    // TODO: move files, show alert if there is no path in the settings... or open the settings directly?

                    if (media.exists()) {
                        boolean error = false;

                        try {
                            Path desiredFilePath = new File(settingsController.getMoveDestination() + File.separator + media.getName()).toPath();
                            Path newFilePath = Files.move(media.toPath(), desiredFilePath);
                            LOGGER.debug(media + " is moved to " + newFilePath);
                        } catch (IOException e) {
                            LOGGER.error("Moving file {} failed. {}", media, e);
                            error = true;
                        }

                        if (!error) {
                            Platform.runLater(() -> {
                                movedFilesCount.set(movedFilesCount.get() + 1);
                                playList.getItems().remove(media);
                                LOGGER.debug("Moved: {}", media);
                            });
                        }
                    }
                }
            }
        }
    }

    private void refreshMediaStatus() {
        toBeDeletedList.clear();
        toBeMovedList.clear();

        for (KnkMedia knkMedia : playList.getItems()) {
            LOGGER.debug(knkMedia);

            String status = knkMedia.getStatus();

            if (status.equals(MEDIA_STATUS.DELETE.getStatus())) {
                toBeDeletedList.add(knkMedia);
            } else if (status.equals(MEDIA_STATUS.MOVE.getStatus())) {
                toBeMovedList.add(knkMedia);
            }
        }
    }

    /**
     * Creates menu items for the context menu for each row in the playlist.
     */
    private void createMenuItemsForContextMenuInPlayList(TableRow<KnkMedia> row, ContextMenu rowMenu) {
        // copy filename to clipboard
        CustomMenuItem copyFileNameMenuItem = createCopyFileNameToClipBoardMenuItem(row);

        // copy absolute path to clipboard
        CustomMenuItem copyFullPathMenuItem = createCopyFullPathToClipBoardMenuItem(row);

        // open in explorer
        CustomMenuItem openExplorerMenuItem = createOpenExplorerMenuItem(row);

        // remove media item from playlist
        CustomMenuItem removeMenuItem = createMenuItem("Removes this file from playlist.",
                "Remove from playlist", event -> removeItem(row.getItem(), false), "removeMenuItem");

        // mark media item to delete from hard drive
        CustomMenuItem deleteMenuItem = createMenuItem("Marks this file to be deleted directly from hard drive.",
                "Delete", event -> markMediaForDeletion(row.getItem(), false), "deleteMenuItem");

        // mark media item to move this file to another destination
        CustomMenuItem moveMenuItem = createMenuItem("Marks this file to be moved to another destination.",
                "Move", event -> markMediaForMoving(row.getItem(), false), "moveMenuItem");

        rowMenu.getItems().addAll(copyFileNameMenuItem, copyFullPathMenuItem, openExplorerMenuItem, removeMenuItem, deleteMenuItem, moveMenuItem);
    }


    /**
     * Creates a menu item which copies the file name to the clipboard.
     *
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

    private CustomMenuItem createOpenExplorerMenuItem(TableRow<KnkMedia> row) {
        Label label = GUIUtil.createLabelWithTooltip("Open this item in explorer", "Open in explorer");
        CustomMenuItem menuItem = new CustomMenuItem(label);
        menuItem.getStyleClass().add("openExplorerMenuItem");

        menuItem.setOnAction(event -> {
            try {
                switch (OSUtils.getOS()) {
                    case WINDOWS -> Runtime.getRuntime().exec("explorer /select, " + row.getItem().getAbsolutePath());
                    case LINUX -> Runtime.getRuntime().exec("xdg-open " + row.getItem().getAbsolutePath());
                    case MAC -> Runtime.getRuntime().exec("open -R " + row.getItem().getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        });

        return menuItem;
    }

    private void markMediaForDeletion(KnkMedia media) {
        markMediaForDeletion(media, true);
    }

    /**
     * Sets the status of the media to be deleted.
     *
     * @param skip skips to the next media file if true
     */
    private void markMediaForDeletion(KnkMedia media, boolean skip) {
        LOGGER.debug("Mark for deletion: {}", media);
        if (!isPlaylistEmpty() && media != null) {
            media.setStatus(MEDIA_STATUS.DELETE);
        }
        playList.refresh();
        if (skip) {
            next();
        }
    }

    private void markMediaForMoving(KnkMedia media) {
        markMediaForMoving(media, true);
    }

    /**
     * Sets the status of the media to be moved to another location.
     *
     * @param skip skips to the next media file in playlist if true
     */
    private void markMediaForMoving(KnkMedia media, boolean skip) {
        LOGGER.debug("Mark for moving: {}", media);
        if (!isPlaylistEmpty() && media != null) {
            media.setStatus(MEDIA_STATUS.MOVE);
        }
        playList.refresh();
        if (skip) {
            next();
        }
    }

    /**
     * Creates a menu item.
     *
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
     * @return true if item was removed from playlist successfully, otherwise false
     */
    private boolean removeItem(KnkMedia media) {
        return removeItem(media, true);
    }

    /**
     * Removes item from playlist.
     *
     * @param skip skip to the next media in playlist if true
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

    private void play() {
        if (embeddedMediaPlayer != null) {

            switch (embeddedMediaPlayer.status().state()) {
                case PLAYING, PAUSED -> embeddedMediaPlayer.controls().pause();
                default -> {
                    if (currentPlayingMedia == null) {
                        currentPlayingMedia = playList.getItems().get(0);
                        currentPlayingIndex.set(playList.getItems().indexOf(currentPlayingMedia));
                    }
                    embeddedMediaPlayer.media().play(currentPlayingMedia.getAbsolutePath());
                }
            }
        }
    }

    private void play(KnkMedia knkMedia) {
        if (embeddedMediaPlayer != null) {
            if (knkMedia != null) {
                embeddedMediaPlayer.media().play(knkMedia.getAbsolutePath());
                playList.getSelectionModel().select(knkMedia);
                currentPlayingMedia = knkMedia;
                currentPlayingIndex.set(playList.getItems().indexOf(currentPlayingMedia));
            }
        }
    }

    /**
     * Skips to the previous media file in the playlist and plays it.
     */
    private void previous() {
        ObservableList<KnkMedia> medias = playList.getItems();
        if (currentPlayingMedia != null && medias != null && !medias.isEmpty()) {
            int index = medias.indexOf(currentPlayingMedia);
            KnkMedia previousMedia = medias.get(index - 1);

            if (previousMedia != null) {
                play(previousMedia);
            } else {
                // No previous media file exists
                // TODO: show notification about reaching the top of the playlist and create a Binding and disable the prev button
            }
        }
    }

    /**
     * Skips to the next media file and plays it.
     */
    private void next() {
        ObservableList<KnkMedia> medias = playList.getItems();
        if (currentPlayingMedia != null && medias != null && !medias.isEmpty()) {
            int index = medias.indexOf(currentPlayingMedia);
            KnkMedia nextMedia = medias.get(index + 1);

            if (nextMedia != null && medias.size() >= index + 1) {
                play(nextMedia);
            } else {
                // No next media file exists
                // TODO show a notification a la "End of playlist. Wanna process all files?" and create a binding for disabling the next button
            }
        }
    }

    private void stop() {
        if (embeddedMediaPlayer != null) {
            embeddedMediaPlayer.controls().stop();
            timeSlider.setValue(0);
            playPauseBtn.setText("Play");
            playPauseBtn.setGraphic(playIcon);
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

        boolean onlyUnprocessedFilesInPlaylist = isOnlyUnprocessedFilesInPlaylist();

        Alert alert = AlertUtils.confirm("Empty playlist", "Are you sure to continue?",
                "All items will be removed from the playlist." + (!onlyUnprocessedFilesInPlaylist
                        ? " There are files in the list whose status is changed. If you continue, no files will be deleted or moved." : ""));
        Optional<ButtonType> buttonType = alert.showAndWait();

        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
            playList.getItems().clear();
        }
    }

    private boolean isOnlyUnprocessedFilesInPlaylist() {
        boolean onlyUnprocessedFilesInPlaylist = true;
        for (KnkMedia media : playList.getItems()) {
            if (!MEDIA_STATUS.UNPROCESSED.equals(MEDIA_STATUS.valueOf(media.getStatus()))) {
                onlyUnprocessedFilesInPlaylist = false;
                break;
            }
        }
        return onlyUnprocessedFilesInPlaylist;
    }

    public EmbeddedMediaPlayer getEmbeddedMediaPlayer() {
        return embeddedMediaPlayer;
    }

    public int getDeletedFilesCount() {
        return deletedFilesCount.get();
    }

    public SimpleIntegerProperty deletedFilesCountProperty() {
        return deletedFilesCount;
    }

    public void setDeletedFilesCount(int deletedFilesCount) {
        this.deletedFilesCount.set(deletedFilesCount);
    }

    public long getDeletedFilesSize() {
        return deletedFilesSize.get();
    }

    public SimpleLongProperty deletedFilesSizeProperty() {
        return deletedFilesSize;
    }

    public void setDeletedFilesSize(long deletedFilesSize) {
        this.deletedFilesSize.set(deletedFilesSize);
    }

    public int getCurrentPlayingIndex() {
        return currentPlayingIndex.get();
    }

    public SimpleIntegerProperty currentPlayingIndexProperty() {
        return currentPlayingIndex;
    }

    public void setCurrentPlayingIndex(int currentPlayingIndex) {
        this.currentPlayingIndex.set(currentPlayingIndex);
    }

}
