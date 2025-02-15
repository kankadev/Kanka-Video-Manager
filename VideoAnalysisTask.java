import javafx.concurrent.Task;

// Videoanalyse-Logik in eigenen Thread auslagern
private void analyzeVideo(KnkMedia media) {
    Task<Void> analyzeTask = new Task<>() {
        @Override
        protected Void call() throws Exception {
            updateMessage("Analysiere Video: " + media.getName());
            
            // Zeitaufwendige Analyse durchführen
            personDetector.analyze(media);

            updateMessage("Analyse abgeschlossen: " + media.getName());
            return null;
        }
    };

    // Fortschritt (optional) überwachen
    analyzeTask.setOnSucceeded(e -> LOGGER.info("Analyse abgeschlossen."));
    analyzeTask.setOnFailed(e -> LOGGER.error("Fehler bei der Analyse", analyzeTask.getException()));

    // Task in einem eigenen Thread starten
    Thread analyzeThread = new Thread(analyzeTask);
    analyzeThread.setDaemon(true);
    analyzeThread.start();
}