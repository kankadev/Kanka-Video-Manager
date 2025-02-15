package dev.kanka.kankavideomanager.utils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.*;

public class PersonDetector {

    private final CascadeClassifier faceDetector;

    public PersonDetector() {

        String cascadePath = "/lib/opencv/build/etc/haarcascades/haarcascade_frontalface_default.xml";
        try (InputStream cascadeStream = getClass().getResourceAsStream(cascadePath)) {
            if (cascadeStream != null) {
                nu.pattern.OpenCV.loadLocally();
                File tempFile = createTempFile(cascadeStream);
                faceDetector = new CascadeClassifier(tempFile.getAbsolutePath());
                tempFile.delete();
            } else {
                throw new RuntimeException("Cascade file not found: " + cascadePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary cascade file", e);
        }
    }

    private File createTempFile(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile("cascade", ".xml");
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    public int detectPersonsInVideo(String videoPath) {
        VideoCapture video = new VideoCapture(videoPath);
        Mat frame = new Mat();
        int maxDetectedPersons = 0;

        while (video.read(frame)) {
            // Führe die Gesichtserkennung für jeden Frame durch
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(frame, faces, 1.1, 4, 0, new Size(30, 30), new Size());

            // Zähle die Anzahl der erkannten Gesichter
            int detectedPersons = faces.toArray().length;
            maxDetectedPersons = Math.max(maxDetectedPersons, detectedPersons);
        }

        video.release();

        return maxDetectedPersons;
    }
}