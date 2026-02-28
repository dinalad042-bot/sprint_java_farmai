package tn.esprit.farmai.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.*;
import javafx.stage.Stage;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.FaceEnrollmentService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.SessionManager;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Controller for the Face Login pop-up window.
 *
 * Flow:
 * 1. Camera opens and detects faces using Haar cascade.
 * 2. On each detected face, the grayscale ROI is passed to
 * FaceEnrollmentService.recognizeFace().
 * 3. On match → close window, set session, navigate to dashboard.
 * 4. After TIMEOUT_SECONDS with no match → stop camera, show error.
 */
public class FaceLoginController implements Initializable {

    // -----------------------------------------------------------------------
    // FXML
    // -----------------------------------------------------------------------
    @FXML
    private ImageView cameraFeed;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressBar timeoutBar;

    // -----------------------------------------------------------------------
    // Config
    // -----------------------------------------------------------------------
    private static final int FPS = 15;
    private static final int TIMEOUT_SECONDS = 15;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------
    private OpenCVFrameGrabber grabber;
    private CascadeClassifier faceDetector;
    private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
    private final Java2DFrameConverter imageConverter = new Java2DFrameConverter();
    private final FaceEnrollmentService enrollService = new FaceEnrollmentService();

    private ScheduledExecutorService timer;
    private ScheduledExecutorService countdown;
    private final AtomicBoolean loginSucceeded = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    // seconds elapsed (for progress bar)
    private volatile int elapsed = 0;

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCascade();
        startCamera();
        startCountdown();
    }

    private void loadCascade() {
        try {
            String resourcePath = "/tn/esprit/farmai/cascade/haarcascade_frontalface_default.xml";
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                setStatus("❌ Cascade XML introuvable", "red");
                return;
            }
            Path tmp = Files.createTempFile("haarcascade_", ".xml");
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            faceDetector = new CascadeClassifier(tmp.toAbsolutePath().toString());
        } catch (Exception e) {
            setStatus("❌ Erreur chargement cascade: " + e.getMessage(), "red");
        }
    }

    // -----------------------------------------------------------------------
    // Camera
    // -----------------------------------------------------------------------

    private void startCamera() {
        try {
            grabber = new OpenCVFrameGrabber(0);
            grabber.setImageWidth(480);
            grabber.setImageHeight(360);
            grabber.start();

            setStatus("📷 Recherche de votre visage…", "#4ecca3");

            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(this::processFrame, 0, 1000L / FPS, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            setStatus("❌ Impossible d'ouvrir la caméra: " + e.getMessage(), "red");
        }
    }

    private void startCountdown() {
        countdown = Executors.newSingleThreadScheduledExecutor();
        countdown.scheduleAtFixedRate(() -> {
            if (stopped.get())
                return;
            elapsed++;
            double remaining = (double) (TIMEOUT_SECONDS - elapsed) / TIMEOUT_SECONDS;
            Platform.runLater(() -> timeoutBar.setProgress(Math.max(0, remaining)));

            if (elapsed >= TIMEOUT_SECONDS && !loginSucceeded.get()) {
                stopAll();
                setStatus("⏱ Délai dépassé — visage non reconnu.", "red");
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    // -----------------------------------------------------------------------
    // Core frame processing loop (background thread)
    // -----------------------------------------------------------------------

    private void processFrame() {
        if (stopped.get() || grabber == null)
            return;

        try {
            Frame frame = grabber.grab();
            if (frame == null || frame.image == null)
                return;

            Mat colorMat = converter.convert(frame);
            if (colorMat == null || colorMat.empty())
                return;

            // Grayscale + equalize
            Mat gray = new Mat();
            cvtColor(colorMat, gray, COLOR_BGR2GRAY);
            equalizeHist(gray, gray);

            // Detect faces
            RectVector faces = new RectVector();
            if (faceDetector != null && !faceDetector.empty()) {
                faceDetector.detectMultiScale(
                        gray, faces, 1.1, 3, 0,
                        new Size(80, 80), new Size(0, 0));
            }

            // Draw boxes on color frame for preview
            for (long i = 0; i < faces.size(); i++) {
                Rect r = faces.get(i);
                rectangle(colorMat, r, new Scalar(0, 230, 118, 255), 2, LINE_8, 0);
            }

            // Push preview to UI
            Image fxImg = toFxImage(imageConverter.convert(converter.convert(colorMat)));
            Platform.runLater(() -> cameraFeed.setImage(fxImg));

            // Try to recognize the first detected face
            if (faces.size() > 0 && !loginSucceeded.get()) {
                Rect faceRect = faces.get(0);
                Mat faceROI = new Mat(gray, faceRect);

                Optional<User> match = enrollService.recognizeFace(faceROI);

                if (match.isPresent()) {
                    loginSucceeded.set(true);
                    User user = match.get();
                    stopAll();
                    Platform.runLater(() -> {
                        setStatus("✅ Bienvenue, " + user.getPrenom() + "!", "#4ecca3");
                        SessionManager.getInstance().setCurrentUser(user);
                        // Navigate to dashboard using the stage of the pop-up
                        Stage stage = (Stage) cameraFeed.getScene().getWindow();
                        stage.close();
                        // Open main app window for this user
                        NavigationUtil.navigateToDashboard(getPrimaryStage());
                    });
                } else {
                    setStatus("🔍 Visage détecté… vérification en cours", "#ffa500");
                }
                faceROI.release();
            }

            colorMat.release();
            gray.release();

        } catch (Exception e) {
            System.err.println("[FaceLoginController] frame error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Cancel
    // -----------------------------------------------------------------------

    @FXML
    public void handleCancel() {
        stopAll();
        Platform.runLater(() -> {
            Stage stage = (Stage) cameraFeed.getScene().getWindow();
            stage.close();
        });
    }

    // -----------------------------------------------------------------------
    // Cleanup
    // -----------------------------------------------------------------------

    private void stopAll() {
        if (stopped.getAndSet(true))
            return; // already stopped
        shutdownExecutor(timer);
        shutdownExecutor(countdown);
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception ignored) {
            }
            grabber = null;
        }
    }

    private void shutdownExecutor(ScheduledExecutorService ex) {
        if (ex != null && !ex.isShutdown()) {
            ex.shutdown();
            try {
                ex.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Call from stage.setOnCloseRequest */
    public void cleanup() {
        stopAll();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void setStatus(String message, String color) {
        Platform.runLater(() -> {
            statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px;");
            statusLabel.setText(message);
        });
    }

    private Image toFxImage(java.awt.image.BufferedImage bi) {
        if (bi == null)
            return null;
        WritableImage wi = new WritableImage(bi.getWidth(), bi.getHeight());
        PixelWriter pw = wi.getPixelWriter();
        for (int y = 0; y < bi.getHeight(); y++)
            for (int x = 0; x < bi.getWidth(); x++)
                pw.setArgb(x, y, bi.getRGB(x, y));
        return wi;
    }

    /**
     * Walks up the JavaFX window list to find the primary (login) stage.
     * If this pop-up is the only stage, returns null and NavigationUtil
     * will create a new primary window.
     */
    private Stage getPrimaryStage() {
        for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
            if (w instanceof Stage s && s.isShowing() &&
                    !s.equals(cameraFeed.getScene().getWindow())) {
                return s;
            }
        }
        // Fall back: create a new stage
        return new Stage();
    }
}
