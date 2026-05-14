package tn.esprit.farmai.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.FaceEnrollmentService;
import tn.esprit.farmai.utils.SessionManager;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * FaceRecognitionController — Real-time face detection using OpenCV Haar
 * Cascades.
 *
 * Threading model:
 * - Camera capture runs on a ScheduledExecutorService (background thread).
 * - All JavaFX node mutations are delegated to Platform.runLater().
 */
public class FaceRecognitionController implements Initializable {

    // -----------------------------------------------------------------------
    // FXML nodes
    // -----------------------------------------------------------------------
    @FXML
    private ImageView cameraFeed;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnEnroll;
    @FXML
    private Label statusLabel;

    // -----------------------------------------------------------------------
    // OpenCV fields
    // -----------------------------------------------------------------------
    private OpenCVFrameGrabber grabber;
    private CascadeClassifier faceDetector;
    private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
    private final Java2DFrameConverter imageConverter = new Java2DFrameConverter();

    // -----------------------------------------------------------------------
    // Threading
    // -----------------------------------------------------------------------
    private ScheduledExecutorService timer;

    /** Target frame rate for the capture loop */
    private static final int FPS = 15;

    private final FaceEnrollmentService enrollService = new FaceEnrollmentService();
    // Keeps the most recent detected face gray ROI for enrollment
    private volatile Mat lastFaceGray = null;

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCascadeClassifier();
    }

    /**
     * Copies the Haar cascade XML from the JAR resources to a temp file,
     * then loads it with OpenCV (which needs a real filesystem path).
     */
    private void loadCascadeClassifier() {
        try {
            String resourcePath = "/tn/esprit/farmai/cascade/haarcascade_frontalface_default.xml";
            InputStream is = getClass().getResourceAsStream(resourcePath);

            if (is == null) {
                setStatus("❌ Cascade XML not found — see README", "red");
                return;
            }

            Path tempFile = Files.createTempFile("haarcascade_", ".xml");
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();

            faceDetector = new CascadeClassifier(tempFile.toAbsolutePath().toString());

            if (faceDetector.empty()) {
                setStatus("❌ Cascade classifier failed to load!", "red");
            } else {
                setStatus("✅ Classifier ready — press Start", "#4ecca3");
            }

        } catch (Exception e) {
            setStatus("❌ Error: " + e.getMessage(), "red");
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------------
    // Camera control — called by FXML button handlers
    // -----------------------------------------------------------------------

    @FXML
    public void startCamera() {
        if (grabber != null)
            return; // already running

        try {
            grabber = new OpenCVFrameGrabber(0); // 0 = first/default webcam
            grabber.setImageWidth(640);
            grabber.setImageHeight(480);
            grabber.start();

            setStatus("📷 Camera active — scanning for faces…", "#4ecca3");
            Platform.runLater(() -> {
                btnStart.setDisable(true);
                btnStop.setDisable(false);
            });

            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(
                    this::captureAndProcessFrame,
                    0,
                    1000L / FPS,
                    TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            setStatus("❌ Cannot open camera: " + e.getMessage(), "red");
            e.printStackTrace();
        }
    }

    @FXML
    public void stopCamera() {
        stopCameraInternal();
        setStatus("⏹ Camera stopped.", "#a8a8b3");
        Platform.runLater(() -> {
            btnStart.setDisable(false);
            btnStop.setDisable(true);
            cameraFeed.setImage(null);
        });
    }

    // -----------------------------------------------------------------------
    // Core capture + detection loop (runs on background thread)
    // -----------------------------------------------------------------------

    private void captureAndProcessFrame() {
        if (grabber == null)
            return;

        try {
            Frame frame = grabber.grab();
            if (frame == null || frame.image == null)
                return;

            Mat colorMat = converter.convert(frame);
            if (colorMat == null || colorMat.empty())
                return;

            // ---- Detection ------------------------------------------------
            Mat grayMat = new Mat();
            cvtColor(colorMat, grayMat, COLOR_BGR2GRAY);
            equalizeHist(grayMat, grayMat); // normalise brightness

            RectVector faces = new RectVector();
            if (faceDetector != null && !faceDetector.empty()) {
                faceDetector.detectMultiScale(
                        grayMat,
                        faces,
                        1.1, // scaleFactor
                        3, // minNeighbors (raise to reduce false positives)
                        0,
                        new Size(80, 80), // minSize
                        new Size(0, 0) // maxSize (unlimited)
                );
            }

            // ---- Draw bounding boxes --------------------------------------
            for (long i = 0; i < faces.size(); i++) {
                Rect face = faces.get(i);
                // Green border box
                rectangle(
                        colorMat, face,
                        new Scalar(0, 230, 118, 255), // BGRA green
                        2, LINE_8, 0);
                // Label above the box
                putText(
                        colorMat,
                        "Face " + (i + 1),
                        new Point(face.x(), Math.max(0, face.y() - 8)),
                        FONT_HERSHEY_SIMPLEX, 0.6,
                        new Scalar(0, 230, 118, 255),
                        2, LINE_8, false);
            }

            // ---- Convert to JavaFX Image ----------------------------------
            Frame processed = converter.convert(colorMat);
            java.awt.image.BufferedImage buffered = imageConverter.convert(processed);
            if (buffered == null)
                return;

            Image fxImage = toFxImage(buffered);
            long faceCount = faces.size();

            Platform.runLater(() -> {
                cameraFeed.setImage(fxImage);
                if (faceCount == 0) {
                    statusLabel.setStyle("-fx-text-fill: #a8a8b3; -fx-font-size: 13px;");
                    statusLabel.setText("🔍 No face detected");
                } else {
                    statusLabel.setStyle("-fx-text-fill: #4ecca3; -fx-font-size: 13px;");
                    statusLabel.setText("✅ " + faceCount + " face(s) detected");
                }
            });

            // ---- Store last face ROI for enrollment -----------------------
            if (faces.size() > 0) {
                Rect firstFace = faces.get(0);
                Mat faceROI = new Mat(grayMat, firstFace);
                if (lastFaceGray != null)
                    lastFaceGray.release();
                lastFaceGray = faceROI.clone();
                faceROI.release();
            }

            // Release native memory
            colorMat.release();
            grayMat.release();

        } catch (Exception e) {
            System.err.println("[FaceRecognitionController] Frame error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Face Enrollment — called by "💾 Enregistrer mon visage" button
    // -----------------------------------------------------------------------

    @FXML
    public void enrollFace() {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current == null) {
            setStatus("❌ Vous devez être connecté pour enregistrer votre visage.", "red");
            return;
        }
        if (lastFaceGray == null) {
            setStatus("⚠️ Démarrez la caméra et placez votre visage dans le cadre.", "#ffa500");
            return;
        }
        setStatus("⏳ Enregistrement en cours…", "#ffa500");
        // Run enrollment on a background thread
        Mat faceSnapshot = lastFaceGray.clone();
        new Thread(() -> {
            try {
                enrollService.enrollFace(current.getIdUser(), faceSnapshot);
                faceSnapshot.release();
                setStatus("✅ Visage enregistré pour " + current.getPrenom() + "!", "#4ecca3");
            } catch (Exception e) {
                faceSnapshot.release();
                setStatus("❌ Erreur d'enregistrement: " + e.getMessage(), "red");
                e.printStackTrace();
            }
        }, "face-enroll").start();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Converts a BufferedImage to a JavaFX WritableImage pixel-by-pixel.
     * Avoids a SwingFXUtils dependency (which requires javafx.swing module).
     */
    private Image toFxImage(java.awt.image.BufferedImage bi) {
        WritableImage wi = new WritableImage(bi.getWidth(), bi.getHeight());
        PixelWriter pw = wi.getPixelWriter();
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                pw.setArgb(x, y, bi.getRGB(x, y));
            }
        }
        return wi;
    }

    private void stopCameraInternal() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try {
                timer.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            timer = null;
        }
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception ignored) {
            }
            grabber = null;
        }
    }

    private void setStatus(String message, String color) {
        Platform.runLater(() -> {
            statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px;");
            statusLabel.setText(message);
        });
    }

    /**
     * Call this from your stage's onCloseRequest to cleanly release the webcam.
     * Example: stage.setOnCloseRequest(e -> controller.cleanup());
     */
    public void cleanup() {
        stopCameraInternal();
    }
}
