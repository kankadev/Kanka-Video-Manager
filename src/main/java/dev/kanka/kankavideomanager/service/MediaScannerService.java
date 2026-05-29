package dev.kanka.kankavideomanager.service;

import dev.kanka.kankavideomanager.pojo.KnkMedia;
import dev.kanka.kankavideomanager.constants.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MediaScannerService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static MediaScannerService instance;
    
    private final MediaPlayerFactory mediaPlayerFactory;
    private final ExecutorService executorService;
    
    private MediaScannerService() {
        this.mediaPlayerFactory = new MediaPlayerFactory("--no-audio", "--no-video");
        this.executorService = Executors.newFixedThreadPool(Constants.SCANNER_THREAD_POOL_SIZE);
    }
    
    public static synchronized MediaScannerService getInstance() {
        if (instance == null) {
            instance = new MediaScannerService();
        }
        return instance;
    }
    
    public void scanMediaAsync(KnkMedia media, DurationCallback callback) {
        executorService.submit(() -> {
            MediaPlayer mediaPlayer = null;
            try {
                mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer();
                mediaPlayer.media().play(media.getAbsolutePath());
                
                // Wait for media to be parsed (check if duration becomes available)
                int maxWait = Constants.SCANNER_MAX_WAIT_SECONDS; // seconds
                int waited = 0;
                long duration = 0;
                while (waited < maxWait) {
                    duration = mediaPlayer.media().info().duration();
                    if (duration > 0) {
                        break;
                    }
                    TimeUnit.MILLISECONDS.sleep(Constants.SCANNER_POLL_INTERVAL_MS);
                    waited++;
                }
                
                callback.onDurationFound(duration);
            } catch (Exception e) {
                LOGGER.error("Error scanning media: {}", media, e);
                callback.onDurationFound(0);
            } finally {
                if (mediaPlayer != null) {
                    mediaPlayer.controls().stop();
                    mediaPlayer.release();
                }
            }
        });
    }
    
    public void shutdown() {
        executorService.shutdown();
        mediaPlayerFactory.release();
    }
    
    public interface DurationCallback {
        void onDurationFound(long duration);
    }
}
