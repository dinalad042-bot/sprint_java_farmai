package tn.esprit.farmai.utils;

import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class for centralized input validation across the application.
 * Provides consistent validation rules for emails, CIN, phone numbers, and dates.
 * 
 * This eliminates duplicate validation logic found in:
 * - SignupController
 * - AnalyseDialog
 * - AjoutConseilController
 */
public class ValidationUtils {

    // Regex patterns
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String CIN_REGEX = "\\d{8}";
    private static final String PHONE_REGEX = "[+]?(\\d{8,15})";
    private static final String DATE_REGEX = "^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$";
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern CIN_PATTERN = Pattern.compile(CIN_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    private static final Pattern DATE_PATTERN = Pattern.compile(DATE_REGEX);
    
    // CSS Styles
    private static final String ERROR_BORDER = "-fx-border-color: #D32F2F; -fx-border-width: 2px;";
    private static final String SUCCESS_BORDER = "-fx-border-color: #27ae60; -fx-border-width: 2px;";
    private static final String CLEAR_BORDER = "-fx-border-color: transparent;";

    /**
     * Private constructor - utility class should not be instantiated
     */
    private ValidationUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== BASIC VALIDATION METHODS ====================

    /**
     * Validate email format.
     * 
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate CIN (Carte d'Identité Nationale) format.
     * Must be exactly 8 digits.
     * 
     * @param cin The CIN to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCin(String cin) {
        if (cin == null || cin.trim().isEmpty()) {
            return false;
        }
        return CIN_PATTERN.matcher(cin).matches();
    }

    /**
     * Validate phone number format.
     * Supports optional + prefix and 8-15 digits.
     * 
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate date is within acceptable range.
     * 
     * @param date The date to validate
     * @param minYear Minimum acceptable year (inclusive)
     * @param maxYear Maximum acceptable year (inclusive), use 0 for current year
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(LocalDate date, int minYear, int maxYear) {
        if (date == null) {
            return false;
        }
        
        int currentYear = LocalDate.now().getYear();
        int actualMaxYear = (maxYear == 0) ? currentYear : maxYear;
        
        if (date.getYear() < minYear || date.getYear() > actualMaxYear) {
            return false;
        }
        
        return true;
    }

    /**
     * Validate minimum string length.
     * 
     * @param text The text to validate
     * @param minLength Minimum required length
     * @return true if valid, false otherwise
     */
    public static boolean hasMinLength(String text, int minLength) {
        if (text == null) {
            return false;
        }
        return text.trim().length() >= minLength;
    }

    /**
     * Validate string is not null or empty.
     * 
     * @param text The text to validate
     * @return true if not empty, false otherwise
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    // ==================== REAL-TIME VALIDATION SETUP ====================

    /**
     * Setup real-time email validation on a TextField.
     * Shows red border on invalid, green on valid (only after user has typed something).
     * 
     * @param field The TextField to validate
     */
    public static void setupRealTimeEmailValidation(TextField field) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !field.getText().isEmpty()) { // On focus loss
                if (isValidEmail(field.getText())) {
                    field.setStyle(SUCCESS_BORDER);
                } else {
                    field.setStyle(ERROR_BORDER);
                }
            }
        });
    }

    /**
     * Setup real-time CIN validation on a TextField.
     * Only allows digits and max 8 characters.
     * 
     * @param field The TextField to validate
     */
    public static void setupRealTimeCinValidation(TextField field) {
        // Restrict input to digits only, max 8
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                field.setText(oldVal);
            } else if (newVal.length() > 8) {
                field.setText(oldVal);
            }
        });
        
        // Show border on focus loss
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !field.getText().isEmpty()) {
                if (isValidCin(field.getText())) {
                    field.setStyle(SUCCESS_BORDER);
                } else {
                    field.setStyle(ERROR_BORDER);
                }
            }
        });
    }

    /**
     * Setup real-time phone validation on a TextField.
     * Only allows digits and + character.
     * 
     * @param field The TextField to validate
     */
    public static void setupRealTimePhoneValidation(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[\\d+]*")) {
                field.setText(oldVal);
            }
        });
    }

    /**
     * Setup real-time minimum length validation.
     * 
     * @param field The TextField to validate
     * @param minLength Minimum required length
     */
    public static void setupRealTimeMinLengthValidation(TextField field, int minLength) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() >= minLength) {
                field.setStyle(SUCCESS_BORDER);
            } else if (!newVal.isEmpty()) {
                field.setStyle(ERROR_BORDER);
            }
        });
    }

    // ==================== FIELD ERROR DISPLAY ====================

    /**
     * Show error styling on a field.
     * 
     * @param field The TextField to style
     */
    public static void showFieldError(TextField field) {
        field.setStyle(ERROR_BORDER);
    }

    /**
     * Show success styling on a field.
     * 
     * @param field The TextField to style
     */
    public static void showFieldSuccess(TextField field) {
        field.setStyle(SUCCESS_BORDER);
    }

    /**
     * Clear field styling.
     * 
     * @param field The TextField to clear
     */
    public static void clearFieldError(TextField field) {
        field.setStyle(CLEAR_BORDER);
    }

    // ==================== COMPREHENSIVE VALIDATION ====================

    /**
     * Validate a complete user form and return error messages.
     * 
     * @param email User email
     * @param cin User CIN
     * @param phone User phone (optional)
     * @param password User password
     * @param nom User last name
     * @param prenom User first name
     * @return Array of error messages, empty if all valid
     */
    public static String[] validateUserForm(String email, String cin, String phone, 
                                            String password, String nom, String prenom) {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        if (!isNotEmpty(nom)) {
            errors.add("Le nom est requis");
        }
        
        if (!isNotEmpty(prenom)) {
            errors.add("Le prénom est requis");
        }
        
        if (!isValidEmail(email)) {
            errors.add("L'email n'est pas valide");
        }
        
        if (!isValidCin(cin)) {
            errors.add("Le CIN doit contenir 8 chiffres");
        }
        
        if (!isValidPhone(phone)) {
            errors.add("Le numéro de téléphone n'est pas valide");
        }
        
        if (!hasMinLength(password, 6)) {
            errors.add("Le mot de passe doit contenir au moins 6 caractères");
        }
        
        return errors.toArray(new String[0]);
    }

    /**
     * Validate analysis form data.
     * 
     * @param date The analysis date
     * @param resultat The technical result
     * @param imageUrl The image URL/path
     * @return Array of error messages, empty if all valid
     */
    public static String[] validateAnalyseForm(LocalDate date, String resultat, String imageUrl) {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        if (!isValidDate(date, 2000, 0)) {
            errors.add("La date doit être entre 2000 et aujourd'hui");
        }
        
        if (!hasMinLength(resultat, 5)) {
            errors.add("Le résultat technique doit contenir au moins 5 caractères");
        }
        
        if (!isNotEmpty(imageUrl)) {
            errors.add("L'image est requise");
        }
        
        return errors.toArray(new String[0]);
    }

    /**
     * Validate conseil form data.
     * 
     * @param description The conseil description
     * @return Array of error messages, empty if all valid
     */
    public static String[] validateConseilForm(String description) {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        if (!hasMinLength(description, 10)) {
            errors.add("La description doit contenir au moins 10 caractères");
        }
        
        return errors.toArray(new String[0]);
    }
}
