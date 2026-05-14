# Research Area: Captcha Feature

## Status: 🟢 Complete

## What I Need To Learn
- [x] Which files were modified for captcha
- [x] What dependencies are needed
- [x] How the captcha validation works
- [x] What UI changes are required

## Source
**Commit**: `ad2b90a` - feat: add CAPTCHA to login form  
**Date**: Mon Mar 2 21:21:04 2026 +0100  
**Author**: Aymen Ben Salem

## Files Changed

| File | Change Type | Description |
|------|-------------|-------------|
| `pom.xml` | Modified | Added javafx-swing dependency |
| `module-info.java` | Modified | Added javafx.swing module, changed java.desktop to transitive |
| `LoginController.java` | Modified | Added captcha validation logic |
| `CaptchaUtil.java` | **NEW** | Captcha generation utility class |
| `login.fxml` | Modified | Added captcha UI section |

## Detailed Changes

### 1. pom.xml (Lines 35-40)
**Add dependency:**
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-swing</artifactId>
    <version>17.0.6</version>
</dependency>
```

### 2. module-info.java
**Add requires:**
```java
requires javafx.swing;
```

**Change:**
```java
requires java.desktop;  // OLD
requires transitive java.desktop;  // NEW
```

### 3. NEW FILE: CaptchaUtil.java
**Location**: `src/main/java/tn/esprit/farmai/utils/CaptchaUtil.java`

**Purpose**: Generates random alphanumeric captcha text and creates distorted images.

**Key Methods:**
- `generateCaptchaText()` - Returns 5-6 character random string (excludes confusing chars like 0, O, 1, I, l)
- `createCaptchaImage(String text)` - Creates 160x50 pixel BufferedImage with:
  - Dark background (#1a1a2e) matching UI
  - Green text (#4ecca3) with random rotation
  - Noise lines and dots for security

### 4. LoginController.java Changes

**New Imports:**
```java
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import tn.esprit.farmai.utils.CaptchaUtil;
```

**New Fields:**
```java
@FXML private ImageView captchaImageView;
@FXML private TextField captchaInputField;
@FXML private Button refreshCaptchaButton;
private String currentCaptcha;
```

**New Methods:**
- `generateNewCaptcha()` - Creates new captcha and displays it
- `handleRefreshCaptcha()` - Regenerates captcha on button click

**Modified `handleLogin()`:**
Added captcha validation BEFORE authentication:
```java
String userInput = captchaInputField.getText();
if (userInput == null || !userInput.trim().equalsIgnoreCase(currentCaptcha)) {
    showError("CAPTCHA incorrect. Veuillez réessayer.");
    return;
}
```

### 5. login.fxml Changes

**Replace** "Remember Me" checkbox section with:

```xml
<!-- CAPTCHA Section -->
<VBox spacing="10" alignment="CENTER" maxWidth="320">
    <HBox spacing="10" alignment="CENTER_LEFT">
        <Label text="Vérification CAPTCHA" styleClass="input-label" HBox.hgrow="ALWAYS"/>
        <Button fx:id="refreshCaptchaButton" text="🔄" onAction="#handleRefreshCaptcha"
                style="-fx-background-color: transparent; -fx-text-fill: #4ecca3; -fx-cursor: hand; -fx-font-size: 14px;"/>
    </HBox>
    <HBox spacing="15" alignment="CENTER">
        <StackPane style="-fx-border-color: #4ecca3; -fx-border-radius: 5; -fx-padding: 2;">
            <ImageView fx:id="captchaImageView" fitWidth="160" fitHeight="50" preserveRatio="true"/>
        </StackPane>
        <TextField fx:id="captchaInputField" promptText="Entrez le code" 
                   styleClass="auth-input" HBox.hgrow="ALWAYS"
                   style="-fx-font-size: 14px;"/>
    </HBox>
</VBox>
```

**Add import:**
```xml
<?import javafx.scene.image.ImageView?>
```

## Code Patterns Observed
- Uses `SecureRandom` for cryptographically secure captcha generation
- Uses `SwingFXUtils.toFXImage()` to convert BufferedImage to JavaFX Image
- Case-insensitive captcha validation
- French error messages

## Relevance to Implementation
This feature adds security to the login form by requiring users to complete a visual captcha challenge before authenticating. This prevents automated brute-force attacks.

## Dependencies Required
- `javafx-swing` - For BufferedImage to JavaFX Image conversion
- `java.desktop` (transitive) - For AWT BufferedImage

## Testing Checklist
- [ ] Captcha image displays on login screen
- [ ] Refresh button generates new captcha
- [ ] Correct captcha allows login
- [ ] Incorrect captcha shows error and blocks login
- [ ] Case-insensitive validation works
