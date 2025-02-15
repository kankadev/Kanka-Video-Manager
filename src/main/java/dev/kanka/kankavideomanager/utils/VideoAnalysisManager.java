package dev.kanka.kankavideomanager.utils;

import dev.kanka.kankavideomanager.pojo.KnkMedia;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class VideoAnalysisManager {

    private static final int MAX_THREADS = 4;
    private static final Logger LOGGER = LogManager.getLogger();
    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

    private final PersonDetector personDetector;


    @FXML
    private final ProgressBar progressBar;
    @FXML
    private final TableView<KnkMedia> playList;

    public VideoAnalysisManager(TableView<KnkMedia> playList, ProgressBar progressBar) {
        this.personDetector = new PersonDetector();
        this.playList = playList;
        this.progressBar = progressBar;
    }

    public void analyzeAllVideos() {
        AtomicInteger completedTasks = new AtomicInteger(0); // Fortschritt verfolgen
        int totalTasks = playList.getItems().size();

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS); // ExecutorService erstellen
        try {
            // Analysen für alle Videos starten
            List<Future<Void>> futures = new ArrayList<>();
            for (KnkMedia media : playList.getItems()) {
                futures.add(submitMediaAnalysisTask(media, executor, completedTasks, totalTasks));
            }

            // Warte auf den Abschluss aller Aufgaben
            for (Future<Void> future : futures) {
                future.get(); // Blockiert und wartet, bis jede Aufgabe abgeschlossen ist
            }

            LOGGER.info("Alle Analysen abgeschlossen!");

        } catch (Exception e) {
            LOGGER.error("Ein Fehler ist während der Videoanalyse aufgetreten.", e);
        } finally {
            executor.shutdown(); // ExecutorService herunterfahren
            try {
                if (!executor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // Erzwingt das Herunterfahren
                }
            } catch (InterruptedException ex) {
                executor.shutdownNow(); // Erzwingen bei Interruption
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> progressBar.setVisible(false)); // Fortschrittsbalken ausblenden
        }
    }

    /**
     * Startet die Analyse für ein gegebenes Video.
     */
    private Future<Void> submitMediaAnalysisTask(
            KnkMedia media, ExecutorService executor, AtomicInteger completedTasks, int totalTasks) {

        return executor.submit(() -> {
            try {
                LOGGER.info("Starte Analyse für: {}", media.getName());
                int detectedPersons = personDetector.detectPersonsInVideo(media.getAbsolutePath());
                media.setDetectedPersons(detectedPersons);
                LOGGER.info("Analyse abgeschlossen: {}, Anzahl Personen: {}", media.getName(), media.getDetectedPersons());
            } catch (Exception e) {
                LOGGER.error("Fehler bei der Analyse für {}", media.getName(), e);
            } finally {
                updateProgress(completedTasks, totalTasks);
            }
            return null; // Für Callable<Void> muss null zurückgegeben werden
        });
    }

    /**
     * Aktualisiert den Fortschrittsbalken basierend auf abgeschlossenen Aufgaben.
     */
    private void updateProgress(AtomicInteger completedTasks, int totalTasks) {
        int finished = completedTasks.incrementAndGet();
        Platform.runLater(() -> progressBar.setProgress((double) finished / totalTasks));
    }

}