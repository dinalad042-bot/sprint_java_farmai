# Research Area: Form Validation

## Status: 🟢 Complete

## What I Need To Learn
- Current validation patterns across forms
- Duplicated validation logic
- Missing ValidationUtils class

## Files Examined
- [x] `src/main/java/tn/esprit/farmai/controllers/SignupController.java:180-240` — Validation methods
- [x] `src/main/java/tn/esprit/farmai/utils/AnalyseDialog.java:320-410` — Date validation
- [x] `src/main/java/tn/esprit/farmai/controllers/AjoutConseilController.java:175-210` — Field validation
- [x] `src/main/java/tn/esprit/farmai/controllers/GestionAnalysesController.java` — No validation found

## Findings

### SignupController Validation (Lines 180-240)

**Email Validation:**
```java
private boolean isValidEmail(String email) {
    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    return email.matches(emailRegex);
}
```

**CIN Validation (8 digits):**
```java
private boolean isValidCin(String cin) {
    return cin.matches("\\d{8}");
}
```

**Phone Validation:**
```java
private boolean isValidPhone(String phone) {
    return phone.matches("[+]?\\d{8,15}");
}
```

**Real-time Validation Setup (Lines 242-280):**
```java
private void setupValidation() {
    // Email validation
    emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
        if (!newVal && !emailField.getText().isEmpty()) {
            if (!isValidEmail(emailField.getText())) {
                emailField.setStyle("-fx-border-color: #e74c3c;");
            } else {
                emailField.setStyle("-fx-border-color: #27ae60;");
            }
        }
    });

    // CIN validation (8 digits, numeric only)
    cinField.textProperty().addListener((obs, oldVal, newVal) -> {
        if (!newVal.matches("\\d*")) {
            cinField.setText(oldVal);
        }
        if (newVal.length() > 8) {
            cinField.setText(oldVal);
        }
    });

    // Phone validation
    telephoneField.textProperty().addListener((obs, oldVal, newVal) -> {
        if (!newVal.matches("[\\d+]*")) {
            telephoneField.setText(oldVal);
        }
    });
}
```

### AnalyseDialog Validation (Lines 320-410)

**Date Validation:**
```java
private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
private static final String DATE_REGEX = "^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$";

private boolean validateDate(DatePicker datePicker, Label dateErrorLabel) {
    if (datePicker.getValue() == null) {
        dateErrorLabel.setText("Veuillez sélectionner une date");
        showError(dateErrorLabel);
        return false;
    }
    
    LocalDate selectedDate = datePicker.getValue();
    LocalDate today = LocalDate.now();
    
    if (selectedDate.isAfter(today)) {
        dateErrorLabel.setText("La date ne peut pas être dans le futur");
        showError(dateErrorLabel);
        return false;
    }
    
    if (selectedDate.getYear() < 2000) {
        dateErrorLabel.setText("La date doit être après le 01/01/2000");
        showError(dateErrorLabel);
        return false;
    }
    
    return true;
}
```

**Result Validation:**
```java
String resultat = resultatField.getText().trim();
if (resultat.isEmpty() || resultat.length() < 5) {
    resultatErrorLabel.setText("Le résultat doit contenir au moins 5 caractères");
    showError(resultatErrorLabel);
    resultatField.setStyle("-fx-border-color: #D32F2F; -fx-border-width: 2px;");
    isValid = false;
} else {
    hideError(resultatErrorLabel);
    resultatField.setStyle("-fx-border-color: transparent;");
}
```

### AjoutConseilController Validation (Lines 175-210)

**Field Validation:**
```java
private boolean validateFields() {
    boolean isValid = true;

    // Validate Analyse selection
    if (analyseComboBox.getValue() == null) {
        showError(analyseErrorLabel);
        isValid = false;
    }

    // Validate Priorite selection
    if (prioriteComboBox.getValue() == null) {
        showError(prioriteErrorLabel);
        isValid = false;
    }

    // Validate Description (min 10 characters)
    String description = descriptionConseilField.getText();
    if (description == null || description.trim().length() < 10) {
        showError(descriptionErrorLabel);
        descriptionConseilField.setStyle("-fx-border-color: #D32F2F; -fx-border-width: 2px;");
        isValid = false;
    }

    return isValid;
}
```

**Real-time Validation Listener:**
```java
private void setupValidationListeners() {
    descriptionConseilField.textProperty().addListener((obs, oldVal, newVal) -> {
        int length = newVal != null ? newVal.length() : 0;
        charCountLabel.setText(length + " caractères");

        if (length >= 10) {
            clearError(descriptionErrorLabel);
            descriptionConseilField.setStyle("-fx-border-color: transparent;");
        }
    });
}
```

### Validation Patterns Summary

| Validation | Location | Regex/Pattern |
|------------|----------|---------------|
| Email | SignupController | `^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$` |
| CIN | SignupController | `\d{8}` |
| Phone | SignupController | `[+]?\d{8,15}` |
| Date | AnalyseDialog | `^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$` |
| Min Length | AnalyseDialog, AjoutConseil | String.length() >= N |

### Missing ValidationUtils Class

**No centralized validation utility exists.** Each controller has its own:
- Email regex
- CIN regex
- Date formatter
- Error styling (`-fx-border-color: #e74c3c` vs `#D32F2F`)

## Code Patterns Observed

**Real-time Validation Pattern (SignupController):**
```java
field.focusedProperty().addListener((obs, oldVal, newVal) -> {
    if (!newVal && !field.getText().isEmpty()) {  // On focus loss
        if (!isValid(field.getText())) {
            field.setStyle("-fx-border-color: #e74c3c;");
        } else {
            field.setStyle("-fx-border-color: #27ae60;");
        }
    }
});
```

**Text Input Limiting Pattern:**
```java
field.textProperty().addListener((obs, oldVal, newVal) -> {
    if (!newVal.matches("\\d*")) {  // Only digits
        field.setText(oldVal);
    }
    if (newVal.length() > 8) {  // Max length
        field.setText(oldVal);
    }
});
```

## Relevance to Implementation
**HIGH PRIORITY:**
1. Create ValidationUtils class with centralized regex patterns
2. Standardize error styling colors (use `#D32F2F` consistently)
3. Apply real-time validation pattern from SignupController to all forms
4. Use AlertUtils.showValidationErrors() for displaying multiple errors

**Proposed ValidationUtils API:**
```java
public class ValidationUtils {
    public static boolean isValidEmail(String email);
    public static boolean isValidCin(String cin);  // 8 digits
    public static boolean isValidPhone(String phone);
    public static boolean isValidDate(LocalDate date, int minYear);
    public static void setupRealTimeValidation(TextField field, Pattern pattern);
    public static void showFieldError(TextField field, String errorMessage);
    public static void clearFieldError(TextField field);
}
```

## Status Update
- [x] Examined SignupController validation
- [x] Examined AnalyseDialog date validation
- [x] Examined AjoutConseilController validation
- [x] Documented all regex patterns
- [x] Identified need for ValidationUtils class
- [x] Documented real-time validation pattern
