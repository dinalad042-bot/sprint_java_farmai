package tn.esprit.farmai.services;

import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;

import tn.esprit.farmai.models.User;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Handles face enrollment (save) and face recognition (login) for FarmAi.
 *
 * Strategy:
 * - Enrollment : train an LBPHFaceRecognizer on 1 face image, serialize the
 * model XML to a byte array, and store it as a LONGBLOB in the
 * `face_data` table (INSERT … ON DUPLICATE KEY UPDATE).
 * - Recognition: load every stored model from DB, deserialize each one, run
 * predict() on the incoming face image, and return the User
 * whose model gives a confidence ≤ CONFIDENCE_THRESHOLD.
 */
public class FaceEnrollmentService {

    // Lower = stricter. 80 is a good starting value for LBPH.
    private static final double CONFIDENCE_THRESHOLD = 80.0;

    private final Connection cnx;

    public FaceEnrollmentService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Statement st = cnx.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS face_data (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "face_model LONGBLOB NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY unique_user (user_id), " +
                    "FOREIGN KEY (user_id) REFERENCES user(id_user) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (SQLException e) {
            System.err.println("[FaceEnrollmentService] Error ensuring table exists: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------------
    // Enrollment
    // -----------------------------------------------------------------------

    /**
     * Trains an LBPH model on {@code faceGray} (must be a grayscale, cropped
     * face Mat) and stores the serialized model in the database for the given
     * user. Replaces any existing model for that user.
     *
     * @param userId   id of the user to enroll
     * @param faceGray grayscale face image (already cropped to the face ROI)
     * @throws Exception on OpenCV or DB error
     */
    public void enrollFace(int userId, Mat faceGray) throws Exception {
        // Resize to a standard size for consistency
        Mat resized = new Mat();
        resize(faceGray, resized, new Size(200, 200));

        // Build the training set: one image, label 0
        MatVector images = new MatVector(1);
        images.put(0, resized);
        Mat labels = new Mat(1, 1, org.bytedeco.opencv.global.opencv_core.CV_32SC1);
        labels.ptr(0, 0).putInt(0);

        // Train LBPH
        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.train(images, labels);

        // Serialize to a temp file then read as bytes
        byte[] modelBytes = serializeRecognizer(recognizer);

        // Upsert into DB
        String sql = "INSERT INTO face_data (user_id, face_model) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE face_model = VALUES(face_model), updated_at = CURRENT_TIMESTAMP";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setBytes(2, modelBytes);
            ps.executeUpdate();
        }

        resized.release();
        System.out.println("[FaceEnrollmentService] Face enrolled for userId=" + userId);
    }

    // -----------------------------------------------------------------------
    // Recognition
    // -----------------------------------------------------------------------

    /**
     * Attempts to recognize {@code faceGray} against all enrolled faces in DB.
     *
     * @param faceGray grayscale cropped face image
     * @return Optional containing the matched User, or empty if no match
     * @throws Exception on DB or OpenCV error
     */
    public Optional<User> recognizeFace(Mat faceGray) throws Exception {
        Mat resized = new Mat();
        resize(faceGray, resized, new Size(200, 200));

        // Load all enrolled models
        String sql = "SELECT fd.user_id, fd.face_model, " +
                "u.id_user, u.nom, u.prenom, u.email, u.password, u.cin, " +
                "u.adresse, u.telephone, u.image_url, u.role " +
                "FROM face_data fd JOIN user u ON fd.user_id = u.id_user";

        try (Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                byte[] modelBytes = rs.getBytes("face_model");
                if (modelBytes == null)
                    continue;

                LBPHFaceRecognizer recognizer = deserializeRecognizer(modelBytes);

                int[] label = { 0 };
                double[] confidence = { 0.0 };
                recognizer.predict(resized, label, confidence);

                System.out.printf("[FaceEnrollmentService] userId=%d confidence=%.2f%n",
                        rs.getInt("user_id"), confidence[0]);

                if (confidence[0] <= CONFIDENCE_THRESHOLD) {
                    User user = mapRowToUser(rs);
                    resized.release();
                    return Optional.of(user);
                }
            }
        }

        resized.release();
        return Optional.empty();
    }

    /**
     * Returns true if at least one face is enrolled in the database.
     */
    public boolean hasAnyEnrolledFace() throws SQLException {
        String sql = "SELECT COUNT(*) FROM face_data";
        try (Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Returns true if the given user has an enrolled face.
     */
    public boolean isEnrolled(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM face_data WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private byte[] serializeRecognizer(LBPHFaceRecognizer recognizer) throws IOException {
        Path tmp = Files.createTempFile("lbph_model_", ".xml");
        recognizer.save(tmp.toAbsolutePath().toString());
        byte[] bytes = Files.readAllBytes(tmp);
        Files.deleteIfExists(tmp);
        return bytes;
    }

    private LBPHFaceRecognizer deserializeRecognizer(byte[] modelBytes) throws IOException {
        Path tmp = Files.createTempFile("lbph_model_", ".xml");
        Files.write(tmp, modelBytes);
        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.read(tmp.toAbsolutePath().toString());
        Files.deleteIfExists(tmp);
        return recognizer;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUser(rs.getInt("id_user"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setCin(rs.getString("cin"));
        user.setAdresse(rs.getString("adresse"));
        user.setTelephone(rs.getString("telephone"));
        user.setImageUrl(rs.getString("image_url"));

        String roleStr = rs.getString("role");
        try {
            user.setRole(tn.esprit.farmai.models.Role.valueOf(roleStr));
        } catch (Exception e) {
            user.setRole(tn.esprit.farmai.models.Role.AGRICOLE);
        }
        return user;
    }
}
