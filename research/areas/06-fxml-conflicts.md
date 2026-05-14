# Research Area: FXML View Conflicts

## Status: 🟢 Complete

## What I Need To Learn
- Which FXML files conflict between branches?
- What UI elements differ?
- How to merge FXML files?

## Files Examined
- [x] `feature/expertise-is-alaeddin:views/login.fxml`
- [x] `feature/securite-aymen:views/login.fxml`
- [x] File change lists from all branches

## Findings

### FXML Conflict Matrix

| FXML File | Expertise | Security | Ferme | Resolution |
|-----------|-----------|----------|-------|------------|
| login.fxml | Standard | Face login button | - | **Use Security** |
| admin-dashboard.fxml | Modified | Modified | - | Merge |
| agricole-dashboard.fxml | Modified | Modified | Modified | Merge all |
| expert-dashboard.fxml | Statistics | Basic | - | **Use Expertise** |
| fournisseur-dashboard.fxml | Modified | Modified | - | Merge |
| user-list.fxml | Modified | Modified | - | Merge |
| notifications.fxml | Modified | Modified | - | Merge |
| signup.fxml | Modified | Modified | - | Merge |
| UserLogView.fxml | Added | Added | - | Same |

### login.fxml Analysis

#### Expertise Version
- Standard login form
- Email field
- Password field
- Remember me checkbox
- Login button
- Signup link
- Forgot password link

#### Security Version (ENHANCED)
All Expertise features PLUS:
```xml
<!-- Face Login Button -->
<Button fx:id="faceLoginButton" text="📷 Se connecter avec le visage" 
        styleClass="primary-button" onAction="#handleFaceLogin" prefWidth="320" 
        style="-fx-background-color: #16213e; -fx-text-fill: #4ecca3; 
               -fx-border-color: #4ecca3; -fx-border-type: centered; 
               -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"/>
<Label text="OU" style="-fx-text-fill: #aaa; -fx-font-size: 10px;"/>
<!-- Login Button (standard) -->
```

**RESOLUTION**: Use Security version (has face login button)

### Unique FXML Files per Branch

#### Expertise Branch Unique
| FXML File | Purpose |
|-----------|---------|
| `expert-dashboard.fxml` | Expert dashboard with statistics |
| `gestion-analyses.fxml` | Analysis management |
| `gestion-conseils.fxml` | Advice management |
| `fermier-analyses.fxml` | Farmer analysis view |
| `ajout-conseil.fxml` | Add advice dialog |
| `statistics.fxml` | Statistics dashboard |

#### Security Branch Unique
| FXML File | Purpose |
|-----------|---------|
| `face-login-view.fxml` | Face login popup |
| `face-recognition-view.fxml` | Camera face capture |
| `verification.fxml` | OTP verification |

#### Ferme Branch Unique
| FXML File | Purpose |
|-----------|---------|
| `gestion-fermes.fxml` | Farm management |
| `gestion-animaux.fxml` | Animals management |
| `gestion-plantes.fxml` | Plants management |
| `admin.fxml` | Admin panel |
| `admin_map.fxml` | Map view |

### Merged FXML File List

```
src/main/resources/tn/esprit/farmai/views/
├── login.fxml                    (Security version - face login)
├── signup.fxml                   (Merge)
├── verification.fxml             (Security - OTP)
├── face-login-view.fxml          (Security)
├── face-recognition-view.fxml    (Security)
│
├── admin-dashboard.fxml          (Merge)
├── expert-dashboard.fxml         (Expertise - statistics)
├── agricole-dashboard.fxml       (Merge all three)
├── fournisseur-dashboard.fxml    (Merge)
│
├── gestion-analyses.fxml         (Expertise)
├── gestion-conseils.fxml         (Expertise)
├── fermier-analyses.fxml         (Expertise)
├── ajout-conseil.fxml            (Expertise)
├── statistics.fxml               (Expertise)
│
├── gestion-fermes.fxml           (Ferme)
├── gestion-animaux.fxml          (Ferme)
├── gestion-plantes.fxml          (Ferme)
├── admin.fxml                    (Ferme)
├── admin_map.fxml                (Ferme)
│
├── user-list.fxml                (Merge)
├── notifications.fxml            (Merge)
├── UserLogView.fxml              (Same)
│
└── styles/
    ├── auth.css                  (Same)
    └── dashboard.css             (Same)
```

### CSS Files
Both branches have identical CSS files:
- `auth.css` - Authentication styling
- `dashboard.css` - Dashboard styling

No CSS conflicts detected.

### Resources

#### Security Branch Resources
```
src/main/resources/tn/esprit/farmai/cascade/
└── haarcascade_frontalface_default.xml  (OpenCV face detection)
```

## Relevance to Implementation
FXML conflicts are minimal. The strategy is:
1. Use Security login.fxml (has face login button)
2. Use Expertise expert-dashboard.fxml (has statistics)
3. Include all unique FXML files from each branch
4. Merge dashboard FXMLs that have modifications from multiple branches

## Merged login.fxml
```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<HBox xmlns:fx="http://javafx.com/fxml" 
      fx:controller="tn.esprit.farmai.controllers.LoginController"
      styleClass="login-root" 
      stylesheets="@../styles/auth.css">
    
    <!-- Left Panel - Branding -->
    <VBox styleClass="login-left-panel" HBox.hgrow="ALWAYS" alignment="CENTER" spacing="25">
        <VBox alignment="CENTER" spacing="15">
            <Label text="🌱" style="-fx-font-size: 64px;"/>
            <Label text="FarmAI" styleClass="app-title-label"/>
            <Label text="Agriculture Intelligente" styleClass="app-subtitle-label"/>
        </VBox>
        <VBox alignment="CENTER_LEFT" spacing="15" styleClass="feature-list">
            <HBox alignment="CENTER_LEFT" spacing="12">
                <Label text="✓" styleClass="feature-check-label"/>
                <Label text="Gestion optimisée des cultures" styleClass="feature-text-label"/>
            </HBox>
            <HBox alignment="CENTER_LEFT" spacing="12">
                <Label text="✓" styleClass="feature-check-label"/>
                <Label text="Analyse IA avancée" styleClass="feature-text-label"/>
            </HBox>
            <HBox alignment="CENTER_LEFT" spacing="12">
                <Label text="✓" styleClass="feature-check-label"/>
                <Label text="Suivi en temps réel" styleClass="feature-text-label"/>
            </HBox>
        </VBox>
    </VBox>
    
    <!-- Right Panel - Login Form -->
    <ScrollPane styleClass="signup-scroll" HBox.hgrow="ALWAYS" 
                fitToWidth="true" fitToHeight="true" 
                style="-fx-background-color: #424242;">
        <VBox fx:id="loginContainer" styleClass="login-right-panel" 
              alignment="CENTER" spacing="20">
            <VBox styleClass="login-form-container" spacing="25" alignment="CENTER">
                
                <!-- Header -->
                <VBox alignment="CENTER" spacing="8">
                    <Label text="Connexion" styleClass="form-title-label"/>
                    <Label text="Connectez-vous à votre compte" styleClass="form-subtitle-label"/>
                </VBox>
                
                <!-- Error Label -->
                <Label fx:id="errorLabel" styleClass="error-label" visible="false" wrapText="true"/>
                
                <!-- Email Field -->
                <VBox spacing="8" styleClass="input-group">
                    <Label text="Email" styleClass="input-label"/>
                    <TextField fx:id="emailField" promptText="exemple@email.com" styleClass="auth-input"/>
                </VBox>
                
                <!-- Password Field -->
                <VBox spacing="8" styleClass="input-group">
                    <HBox alignment="CENTER_LEFT">
                        <Label text="Mot de passe" styleClass="input-label" HBox.hgrow="ALWAYS"/>
                        <Hyperlink fx:id="forgotPasswordLink" text="Mot de passe oublié?" 
                                   styleClass="forgot-link" onAction="#handleForgotPassword"/>
                    </HBox>
                    <PasswordField fx:id="passwordField" promptText="••••••••" styleClass="auth-input"/>
                </VBox>
                
                <!-- Remember Me -->
                <HBox alignment="CENTER_LEFT">
                    <CheckBox fx:id="rememberMeCheckbox" text="Se souvenir de moi" 
                              styleClass="remember-checkbox"/>
                </HBox>
                
                <!-- Loading Indicator -->
                <ProgressIndicator fx:id="loadingIndicator" visible="false" 
                                   prefWidth="35" prefHeight="35"/>
                
                <!-- Face Login Button (from Security) -->
                <Button fx:id="faceLoginButton" text="📷 Se connecter avec le visage" 
                        styleClass="primary-button" onAction="#handleFaceLogin" prefWidth="320"
                        style="-fx-background-color: #16213e; -fx-text-fill: #4ecca3; 
                               -fx-border-color: #4ecca3; -fx-border-type: centered; 
                               -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"/>
                
                <Label text="OU" style="-fx-text-fill: #aaa; -fx-font-size: 10px;"/>
                
                <!-- Login Button -->
                <Button fx:id="loginButton" text="Se connecter" 
                        styleClass="primary-button" onAction="#handleLogin" prefWidth="320"/>
                
                <!-- Signup Link -->
                <HBox alignment="CENTER" spacing="8">
                    <Label text="Pas encore de compte?" styleClass="form-subtitle-label"/>
                    <Hyperlink fx:id="signupLink" text="S'inscrire" 
                               styleClass="signup-link" onAction="#handleSignupLink"/>
                </HBox>
                
                <padding>
                    <Insets bottom="20"/>
                </padding>
            </VBox>
        </VBox>
    </ScrollPane>
</HBox>
```

## Status Update
- [x] Analyzed login.fxml differences
- [x] Identified unique FXML files per branch
- [x] Created merged FXML file list
- [x] Created merged login.fxml
