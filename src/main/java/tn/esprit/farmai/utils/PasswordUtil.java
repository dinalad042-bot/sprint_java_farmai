package tn.esprit.farmai.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Supports multiple formats:
 * - PHP bcrypt ($2y$10$...) - for compatibility with Symfony and Java registrations
 * - Legacy salt$hash format - for existing Java users
 * - SHA-256 plain hash - fallback
 */
public class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Hash a password with bcrypt (PHP-compatible)
     * Use this for new registrations to ensure compatibility with web app
     */
    public static String hashPasswordBcrypt(String password) {
        try {
            Class<?> jbcryptClass = Class.forName("org.mindrot.jbcrypt.BCrypt");
            java.lang.reflect.Method hashpw = jbcryptClass.getMethod("hashpw", String.class, String.class);
            java.lang.reflect.Method gensalt = jbcryptClass.getMethod("gensalt", int.class);
            String salt = (String) gensalt.invoke(null, BCRYPT_ROUNDS);
            return (String) hashpw.invoke(null, password, salt);
        } catch (Exception e) {
            System.err.println("jBCrypt hashing failed: " + e.getMessage());
            // Fallback to legacy format
            return hashPassword(password);
        }
    }

    /**
     * Hash a password with a random salt (legacy Java format)
     */
    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            String saltString = Base64.getEncoder().encodeToString(salt);

            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String hashString = Base64.getEncoder().encodeToString(hash);

            return saltString + "$" + hashString;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify password against stored hash (supports multiple formats)
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || password == null) {
            return false;
        }

        // Try PHP bcrypt format ($2y$ or $2a$)
        if (storedHash.startsWith("$2")) {
            return verifyBcrypt(password, storedHash);
        }

        // Try legacy salt$hash format
        if (storedHash.contains("$")) {
            return verifyLegacyHash(password, storedHash);
        }

        // Try plain SHA-256 hash (no salt)
        return verifyPlainHash(password, storedHash);
    }

    /**
     * Verify PHP bcrypt password using jBCrypt library
     * PHP's $2y$ is cryptographically identical to $2a$ - just normalize for jBCrypt
     */
    private static boolean verifyBcrypt(String password, String storedHash) {
        try {
            // Normalize PHP's $2y$ to $2a$ for jBCrypt compatibility
            String normalizedHash = storedHash;
            if (storedHash.startsWith("$2y$")) {
                normalizedHash = "$2a$" + storedHash.substring(4);
            }

            Class<?> jbcryptClass = Class.forName("org.mindrot.jbcrypt.BCrypt");
            java.lang.reflect.Method checkpw = jbcryptClass.getMethod("checkpw", String.class, String.class);
            return (boolean) checkpw.invoke(null, password, normalizedHash);
        } catch (Exception e) {
            System.err.println("jBCrypt verification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verify legacy salt$hash format
     */
    private static boolean verifyLegacyHash(String password, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) {
                return false;
            }

            String saltString = parts[0];
            String expectedHash = parts[1];

            byte[] salt = Base64.getDecoder().decode(saltString);

            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String actualHash = Base64.getEncoder().encodeToString(hash);

            return expectedHash.equals(actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verify plain SHA-256 hash (no salt)
     */
    private static boolean verifyPlainHash(String password, String storedHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equals(storedHash);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    /**
     * Simple hash without salt
     */
    public static String simpleHash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
