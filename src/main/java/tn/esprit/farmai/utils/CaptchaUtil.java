package tn.esprit.farmai.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Utility class for generating random alphanumeric CAPTCHA strings and images.
 */
public class CaptchaUtil {

    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final Random RANDOM = new SecureRandom();

    /**
     * Generates a random alphanumeric string with a length between 5 and 6
     * characters.
     * (Slightly shorter for better image fit)
     * 
     * @return A random alphanumeric string.
     */
    public static String generateCaptchaText() {
        int length = 5 + RANDOM.nextInt(2); // 5 to 6
        StringBuilder captcha = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            captcha.append(ALPHANUMERIC_CHARS.charAt(RANDOM.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return captcha.toString();
    }

    /**
     * Creates a distorted image of the given text.
     * 
     * @param text The text to draw.
     * @return A BufferedImage containing the distorted text.
     */
    public static BufferedImage createCaptchaImage(String text) {
        int width = 160;
        int height = 50;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Background
        g2d.setColor(new Color(26, 26, 46)); // Matching UI background #1a1a2e
        g2d.fillRect(0, 0, width, height);

        // Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Add some noise (lines)
        g2d.setColor(new Color(78, 204, 163, 100)); // #4ecca3 with alpha
        for (int i = 0; i < 5; i++) {
            g2d.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height),
                    RANDOM.nextInt(width), RANDOM.nextInt(height));
        }

        // Font
        Font font = new Font("Monospaced", Font.BOLD, 28);
        g2d.setFont(font);

        char[] chars = text.toCharArray();
        int x = 15;
        for (int i = 0; i < chars.length; i++) {
            // Random color slightly varied from #4ecca3
            g2d.setColor(new Color(78, 204, 163));

            // Random rotation
            double rotation = (RANDOM.nextDouble() - 0.5) * 0.6; // -0.3 to 0.3 radians
            int y = 35 + (int) (RANDOM.nextDouble() * 10 - 5);

            g2d.rotate(rotation, x, y);
            g2d.drawString(String.valueOf(chars[i]), x, y);
            g2d.rotate(-rotation, x, y);

            x += (width - 30) / chars.length;
        }

        // Add more noise (dots)
        for (int i = 0; i < 150; i++) {
            int px = RANDOM.nextInt(width);
            int py = RANDOM.nextInt(height);
            image.setRGB(px, py, new Color(78, 204, 163, 50).getRGB());
        }

        g2d.dispose();
        return image;
    }
}
