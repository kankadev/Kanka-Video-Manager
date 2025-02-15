import javafx.concurrent.Task;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class VideoAnalysisManager {

    private static final int MAX_THREADS = 4; // Anzahl gleichzeitiger Threads
    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TableView<KnkMedia> playList; // Die Tabelle mit allen Videos

    public void analyzeAllVideos() {
        List<KnkMedia> mediaList = playList.getItems(); // Hole alle Videos aus der Playlist
        if (mediaList.isEmpty()) {
            LOGGER.info("Keine Videos in der Playlist vorhanden.");
            return;
        }

        AtomicInteger completedTasks = new AtomicInteger(0); // Zähler für abgeschlossene Aufgaben
        int totalTasks = mediaList.size(); // Gesamte Anzahl der Videos

        // Zeigt an, dass die ProgressBar aktiv ist
        Platform.runLater(() -> {
            progressBar.setVisible(true);
            progressBar.setProgress(0); // Anfangszustand
        });

        // Für jedes Video eine Aufgabe erstellen
        for (KnkMedia media : mediaList) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    LOGGER.info("Starte Analyse für: " + media.getName());
                    personDetector.analyze(media); // Video analysieren (Dein bestehender Code)
                    LOGGER.info("Analyse abgeschlossen für: " + media.getName());

                    // Fortschritt aktualisieren
                    int finished = completedTasks.incrementAndGet();
                    updateProgress(finished, totalTasks);

                    return null;
                }
            };

            // Fortschritt an ProgressBar binden
            task.progressProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> progressBar.setProgress((double) completedTasks.get() / totalTasks));
            });

            task.setOnFailed(e -> LOGGER.error("Fehler bei der Analyse von " + media.getName(), task.getException()));

            // Aufgabe dem Thread-Pool hinzufügen
            executor.submit(task);
        }

        // Stoppe den ProgressBar, wenn alle Aufgaben abgeschlossen sind
        new Thread(() -> {
            try {
                executor.shutdown();
                while (!executor.isTerminated()) {
                    Thread.sleep(100); // Warte, bis alle Tasks abgeschlossen sind
                }
                Platform.runLater(() -> progressBar.setVisible(false)); // Fortschrittsbalken verstecken
                LOGGER.info("Alle Analysen abgeschlossen.");
            } catch (InterruptedException e) {
                LOGGER.error("Fehler beim Warten auf Task-Abschluss", e);
            }
        }).start();
    }
}