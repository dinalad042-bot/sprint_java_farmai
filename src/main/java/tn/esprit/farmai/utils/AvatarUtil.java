package tn.esprit.farmai.utils;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import tn.esprit.farmai.models.User;

import java.io.File;
import java.io.FileInputStream;

/**
 * Utility class for creating circular user avatars.
 * Provides consistent avatar display across the application.
 *
 * Design principles:
 * - ALWAYS show something immediately (colored circle + initials).
 * - NEVER create ImagePattern from an unloaded image (causes "Image not yet
 * loaded").
 * - Use FileInputStream for local files to bypass JavaFX's URL-based cache.
 * - Use progress/error listeners for background-loaded images.
 * - Manage circular clipping inside this class so callers never worry about it.
 */
public class AvatarUtil {

    // ────────────────────────────────────────────────────────────
    // PUBLIC API — creating avatars
    // ────────────────────────────────────────────────────────────

    /**
     * Creates a circular avatar StackPane for use in ListViews, cards, etc.
     *
     * The returned pane immediately shows colored initials, then upgrades
     * to the real image once it loads (no flicker, no shape changes).
     *
     * @param user The user to create avatar for (may be null)
     * @param size The diameter in pixels
     * @return A StackPane containing the circular avatar
     */
    public static StackPane createCircularAvatar(User user, double size) {
        if (user == null) {
            return buildInitialsPane("?", size, Color.web("#90A4AE"));
        }

        Color bgColor = generateColorFromName(user.getFullName());
        String initials = getInitials(user);

        // Background circle (always present — this IS the avatar shape)
        Circle circle = new Circle(size / 2);
        circle.setFill(bgColor);
        circle.setStroke(Color.web("#E0E0E0"));
        circle.setStrokeWidth(2);

        // Initials text (visible until image loads)
        Text initialsText = new Text(initials);
        initialsText.setFill(Color.WHITE);
        initialsText.setStyle(String.format(
                "-fx-font-weight: bold; -fx-font-size: %.0fpx;", size * 0.4));

        StackPane container = new StackPane(circle, initialsText);
        container.setPrefSize(size, size);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);

        // Load user image in background → update circle fill when ready
        loadImageForCircle(circle, initialsText, user);

        return container;
    }

    /**
     * Creates a circular ImageView avatar.
     * Use when you specifically need an ImageView (e.g., FXML-bound elements).
     *
     * @param user The user to create avatar for
     * @param size The width/height in pixels
     * @return An ImageView with permanent circular clip
     */
    public static ImageView createCircularImageView(User user, double size) {
        ImageView imageView = new ImageView();
        applyCircularClip(imageView, size);
        loadUserImageIntoImageView(imageView, user, size);
        return imageView;
    }

    // ────────────────────────────────────────────────────────────
    // PUBLIC API — loading into existing FXML nodes
    // ────────────────────────────────────────────────────────────

    /**
     * Loads user image into an existing Circle (e.g., {@code sidebarAvatar} from
     * FXML).
     *
     * Immediately sets a solid color fill, then upgrades to ImagePattern
     * once the image is confirmed loaded. Never throws "Image not yet loaded".
     *
     * @param circle The FXML Circle to fill
     * @param user   The user whose image to load
     * @return true (loading started or completed)
     */
    public static boolean loadUserImageIntoCircle(Circle circle, User user) {
        if (circle == null || user == null)
            return false;

        // Immediate visible fallback
        circle.setFill(generateColorFromName(user.getFullName()));

        // Start loading
        loadImageForCircle(circle, null, user);
        return true;
    }

    /**
     * Loads user image into an existing ImageView (e.g., header/sidebar from FXML).
     * Applies a permanent circular clip so the avatar is always perfectly round.
     *
     * @param imageView The FXML ImageView to update
     * @param user      The user whose image to load
     * @param size      The desired diameter in pixels
     * @return true (loading started or completed)
     */
    public static boolean loadUserImageIntoImageView(ImageView imageView, User user, double size) {
        if (imageView == null || user == null)
            return false;

        // 1. Force square dimensions + circular clip (survives relayout)
        applyCircularClip(imageView, size);

        String imgUrl = user.getImageUrl();

        // 2. Try local file — synchronous via FileInputStream (no cache)
        if (imgUrl != null && !imgUrl.isEmpty()) {
            String localPath = toLocalPath(imgUrl);
            if (localPath != null) {
                File file = new File(localPath);
                if (file.exists() && file.isFile()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        Image img = new Image(fis, size, size, true, true);
                        if (!img.isError()) {
                            imageView.setImage(img);
                            return true;
                        }
                    } catch (Exception e) {
                        System.err.println("[AvatarUtil] Local image failed for ImageView: " + e.getMessage());
                    }
                }
            }

            // 2b. HTTP URL — background load with cache buster
            if (imgUrl.startsWith("http")) {
                String bustUrl = appendCacheBuster(imgUrl);
                Image img = new Image(bustUrl, size, size, true, true, true);
                imageView.setImage(img);
                // If it fails, switch to API fallback
                img.errorProperty().addListener((obs, wasErr, isErr) -> {
                    if (isErr)
                        Platform.runLater(() -> loadFallbackIntoImageView(imageView, user, size));
                });
                return true;
            }
        }

        // 3. Fallback: UI Avatars API
        loadFallbackIntoImageView(imageView, user, size);
        return true;
    }

    /**
     * Applies a permanent circular clip to an ImageView.
     * Call this once; the clip will persist across image changes and relayout.
     *
     * @param imageView The ImageView to clip
     * @param size      The diameter in pixels
     */
    public static void applyCircularClip(ImageView imageView, double size) {
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(false); // force square → circle clip is always perfect
        imageView.setSmooth(true);

        Circle clip = new Circle(size / 2, size / 2, size / 2);
        imageView.setClip(clip);
    }

    // ────────────────────────────────────────────────────────────
    // PRIVATE — image loading for Circle shapes
    // ────────────────────────────────────────────────────────────

    /**
     * Core image-loading logic for Circle shapes.
     * Local files load synchronously; URLs load asynchronously with listeners.
     * On success, sets the Circle fill to an ImagePattern and hides initials.
     */
    private static void loadImageForCircle(Circle circle, Text initialsText, User user) {
        String imgUrl = user.getImageUrl();

        // 1. Local file — synchronous via FileInputStream (no cache)
        if (imgUrl != null && !imgUrl.isEmpty()) {
            String localPath = toLocalPath(imgUrl);
            if (localPath != null) {
                File file = new File(localPath);
                if (file.exists() && file.isFile()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        Image img = new Image(fis);
                        if (!img.isError()) {
                            circle.setFill(new ImagePattern(img));
                            if (initialsText != null)
                                initialsText.setVisible(false);
                            return; // done
                        }
                    } catch (Exception e) {
                        System.err.println("[AvatarUtil] Local image failed: " + e.getMessage());
                    }
                }
            }

            // 1b. HTTP/HTTPS URL — background load
            if (imgUrl.startsWith("http")) {
                loadUrlIntoCircle(imgUrl, circle, initialsText, user);
                return;
            }
        }

        // 2. No user image → API fallback
        loadApiAvatarIntoCircle(circle, initialsText, user);
    }

    /**
     * Loads an HTTP URL image in background and sets the Circle fill when ready.
     */
    private static void loadUrlIntoCircle(String url, Circle circle, Text initialsText, User user) {
        String bustUrl = appendCacheBuster(url);
        Image img = new Image(bustUrl, 256, 256, true, true, true); // background=true

        img.progressProperty().addListener((obs, oldP, newP) -> {
            if (newP.doubleValue() >= 1.0 && !img.isError()) {
                Platform.runLater(() -> {
                    circle.setFill(new ImagePattern(img));
                    if (initialsText != null)
                        initialsText.setVisible(false);
                });
            }
        });

        img.errorProperty().addListener((obs, wasErr, isErr) -> {
            if (isErr)
                loadApiAvatarIntoCircle(circle, initialsText, user);
        });
    }

    /**
     * Loads a ui-avatars.com API image in background and sets the Circle fill when
     * ready.
     * If this also fails, the solid color fill already set remains visible.
     */
    private static void loadApiAvatarIntoCircle(Circle circle, Text initialsText, User user) {
        try {
            String apiUrl = buildUiAvatarsUrl(user);
            Image img = new Image(apiUrl, 128, 128, true, true, true); // background=true

            img.progressProperty().addListener((obs, oldP, newP) -> {
                if (newP.doubleValue() >= 1.0 && !img.isError()) {
                    Platform.runLater(() -> {
                        circle.setFill(new ImagePattern(img));
                        if (initialsText != null)
                            initialsText.setVisible(false);
                    });
                }
            });

            // On error, keep the solid color + initials (already visible)
            img.errorProperty().addListener((obs, wasErr, isErr) -> {
                if (isErr) {
                    System.err.println("[AvatarUtil] API avatar also failed for: " + user.getFullName());
                }
            });
        } catch (Exception e) {
            // Solid color fallback is already visible — nothing more to do
            System.err.println("[AvatarUtil] Could not start API avatar load: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    // PRIVATE — image loading for ImageView
    // ────────────────────────────────────────────────────────────

    /**
     * Loads the ui-avatars fallback into an ImageView.
     */
    private static void loadFallbackIntoImageView(ImageView imageView, User user, double size) {
        try {
            String apiUrl = buildUiAvatarsUrl(user, (int) size);
            Image fallbackImg = new Image(apiUrl, size, size, true, true, true);
            imageView.setImage(fallbackImg);
            // If even this fails, clear the image (clip keeps the shape)
            fallbackImg.errorProperty().addListener((obs, wasErr, isErr) -> {
                if (isErr)
                    Platform.runLater(() -> imageView.setImage(null));
            });
        } catch (Exception e) {
            System.err.println("[AvatarUtil] ImageView fallback failed: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    // PRIVATE — helper utilities
    // ────────────────────────────────────────────────────────────

    /**
     * Builds a simple initials-only StackPane (no image loading).
     */
    private static StackPane buildInitialsPane(String text, double size, Color bgColor) {
        Circle circle = new Circle(size / 2);
        circle.setFill(bgColor);
        circle.setStroke(Color.web("#E0E0E0"));
        circle.setStrokeWidth(2);

        Text initialsText = new Text(text);
        initialsText.setFill(Color.WHITE);
        initialsText.setStyle(String.format(
                "-fx-font-weight: bold; -fx-font-size: %.0fpx;", size * 0.4));

        StackPane container = new StackPane(circle, initialsText);
        container.setPrefSize(size, size);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    /**
     * Gets initials from user's name.
     */
    private static String getInitials(User user) {
        StringBuilder sb = new StringBuilder();
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            sb.append(Character.toUpperCase(user.getPrenom().charAt(0)));
        }
        if (user.getNom() != null && !user.getNom().isEmpty()) {
            sb.append(Character.toUpperCase(user.getNom().charAt(0)));
        }
        return sb.length() > 0 ? sb.toString() : "?";
    }

    /**
     * Converts an image URL/path to a local filesystem path, or null if remote.
     */
    private static String toLocalPath(String imgUrl) {
        if (imgUrl == null || imgUrl.isEmpty())
            return null;
        if (imgUrl.startsWith("http://") || imgUrl.startsWith("https://"))
            return null;

        if (imgUrl.startsWith("file:")) {
            try {
                java.net.URI uri = new java.net.URI(imgUrl);
                return new File(uri).getAbsolutePath();
            } catch (Exception e) {
                // Fallback: strip file: prefix
                String path = imgUrl.substring(5);
                while (path.startsWith("/"))
                    path = path.substring(1);
                return path;
            }
        }

        return imgUrl; // already a plain filesystem path
    }

    /**
     * Appends a timestamp cache-buster to a URL.
     */
    private static String appendCacheBuster(String url) {
        return url + (url.contains("?") ? "&" : "?") + "_t=" + System.currentTimeMillis();
    }

    /**
     * Builds the UI Avatars API URL (default 128px).
     */
    private static String buildUiAvatarsUrl(User user) {
        return buildUiAvatarsUrl(user, 128);
    }

    /**
     * Builds the UI Avatars API URL with specific size.
     */
    private static String buildUiAvatarsUrl(User user, int size) {
        String name = encodeUrlParam(user.getFullName().trim());
        if (name.isEmpty())
            name = "User";

        String bgColor = generateHexColorFromName(user.getFullName());
        return String.format(
                "https://ui-avatars.com/api/?name=%s&background=%s&color=fff&size=%d&rounded=true",
                name, bgColor, size);
    }

    /**
     * Simple URL-encoding for name parameters.
     */
    private static String encodeUrlParam(String param) {
        if (param == null || param.isEmpty())
            return "";
        return param.replace(" ", "%20")
                .replace("+", "%2B")
                .replace("&", "%26")
                .replace("=", "%3D")
                .replace("?", "%3F");
    }

    /**
     * Generates a consistent Color from a name string (for avatar backgrounds).
     */
    private static Color generateColorFromName(String name) {
        if (name == null || name.isEmpty())
            return Color.web("#90A4AE");

        String[] colors = {
                "#E53935", "#D81B60", "#8E24AA", "#5E35B1",
                "#3949AB", "#1E88E5", "#039BE5", "#00ACC1",
                "#00897B", "#43A047", "#7CB342", "#C0CA33",
                "#FDD835", "#FFB300", "#FB8C00", "#F4511E",
                "#6D4C41", "#757575", "#546E7A"
        };

        int index = Math.abs(name.hashCode()) % colors.length;
        return Color.web(colors[index]);
    }

    /**
     * Generates a hex color code from a name string.
     */
    private static String generateHexColorFromName(String name) {
        Color c = generateColorFromName(name);
        return String.format("%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
