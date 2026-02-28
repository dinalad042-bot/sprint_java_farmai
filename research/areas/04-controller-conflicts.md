# Research Area: Controller Conflicts

## Status: 🟢 Complete

## What I Need To Learn
- Which controllers conflict between branches?
- What are the functional differences?
- How to merge controllers?

## Files Examined
- [x] `feature/expertise-is-alaeddin:LoginController.java`
- [x] `feature/securite-aymen:LoginController.java`
- [x] `feature/expertise-is-alaeddin:ExpertDashboardController.java`
- [x] `feature/securite-aymen:ExpertDashboardController.java`
- [x] `feature/ferme-amen:FermeController.java`

## Findings

### CRITICAL CONFLICT: LoginController.java

#### Expertise Branch Version
**Features**:
- Standard email/password login
- Remember me checkbox
- Forgot password dialog (simple)
- Entrance animations
- Button hover effects
- Email validation

**Code Structure**:
```java
public class LoginController implements Initializable {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink signupLink;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Label errorLabel;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private VBox loginContainer;
    @FXML private ProgressIndicator loadingIndicator;
    
    // Methods: handleLogin(), handleSignupLink(), handleForgotPassword()
    // Utils: showError(), hideError(), setLoading(), playEntranceAnimation()
}
```

#### Security Branch Version (ENHANCED)
**Features**:
- All features from Expertise version PLUS:
- **Face login button** - Opens camera for face recognition
- **OTP verification** - For password reset
- **Email integration** - Sends OTP via email

**Additional Code**:
```java
@FXML private void handleFaceLogin() {
    // Opens FaceLoginController in new window
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/tn/esprit/farmai/views/face-login-view.fxml"));
    // ...
}

@FXML private void handleForgotPassword() {
    // Enhanced with OTP generation and email sending
    String otp = OTPService.generateOTP(email);
    MailingService.sendMail(email, "Réinitialisation de mot de passe", 
        "Votre code de vérification est : " + otp);
    // Navigate to VerificationController
}
```

### RESOLUTION: Use Security Branch Version + Add Face Login
**Reason**: Security version has all Expertise features plus face login and OTP. This is the superior version.

### Controller Conflicts Matrix

| Controller | Expertise | Security | Ferme | Resolution |
|------------|-----------|----------|-------|------------|
| LoginController | Standard login | Face login + OTP | - | **Use Security** |
| AdminDashboardController | Modified | Modified | - | Merge features |
| AgricoleDashboardController | Modified | Modified | Modified | Merge all three |
| ExpertDashboardController | Statistics | Basic | - | Use Expertise |
| FournisseurDashboardController | Modified | Modified | - | Merge |
| UserListController | Modified | Modified | - | Merge |
| NotificationsController | Modified | Modified | - | Merge |
| SignupController | Modified | Modified | - | Merge |
| UserLogController | Added | Added | - | Same - use either |

### Unique Controllers per Branch

#### Expertise Branch Unique Controllers
| Controller | Purpose |
|------------|---------|
| `ExpertDashboardController.java` | Expert dashboard with statistics |
| `GestionAnalysesController.java` | Analysis CRUD management |
| `GestionConseilsController.java` | Advice CRUD management |
| `FermierAnalysesController.java` | Farmer view of analyses |
| `AjoutConseilController.java` | Add advice dialog |
| `StatisticsController.java` | Statistics dashboard |

#### Security Branch Unique Controllers
| Controller | Purpose |
|------------|---------|
| `FaceLoginController.java` | Face recognition login window |
| `FaceRecognitionController.java` | Camera face capture |
| `VerificationController.java` | OTP verification |

#### Ferme Branch Unique Controllers
| Controller | Purpose |
|------------|---------|
| `FermeController.java` | Farm CRUD + Trefle API audit |
| `AnimauxController.java` | Animals management |
| `PlantesController.java` | Plants management |
| `AdminController.java` | Admin panel |
| `AdminMapController.java` | Map view |
| `AgricoIrrigationController.java` | Irrigation AI |

### LoginController Merge Strategy

The Security branch LoginController is superior. However, the FXML needs updating:

#### Required FXML Changes (login.fxml)
```xml
<!-- Add face login button after login button -->
<Button fx:id="faceLoginButton" text="Connexion par visage" 
        onAction="#handleFaceLogin" styleClass="face-login-button"/>
```

#### Required Imports
```java
import tn.esprit.farmai.services.OTPService;
import tn.esprit.farmai.utils.MailingService;
import tn.esprit.farmai.controllers.FaceLoginController;
import tn.esprit.farmai.controllers.VerificationController;
```

### ExpertDashboardController Analysis

#### Expertise Version (Superior)
- Loads dynamic statistics from AnalyseService, ConseilService, FermeService
- Has statistics labels: totalAnalysesLabel, totalConseilsLabel, totalFermesLabel
- Has navigation to: GestionAnalyses, GestionConseils, Statistics
- Uses fade transitions for navigation

#### Security Version (Basic)
- Basic dashboard without statistics
- No analysis/conseil integration

**RESOLUTION**: Use Expertise version

### FermeController Analysis

**Location**: `feature/ferme-amen:src/main/java/tn/esprit/farmai/controllers/FermeController.java`

**Features**:
- Farm CRUD operations
- **Trefle.io API integration** for plant data
- Synergy analysis (animals/plants balance)
- PDF report generation
- Search functionality

**Code Pattern**:
```java
@FXML private void analyserSynergie() {
    // Calls Trefle API for plant data
    // Calculates nitrogen ratio
    // Shows synergy score
}
```

**Note**: Uses snake_case field names from Ferme model - needs update to use expertise Ferme model.

## Relevance to Implementation
Controller conflicts are manageable. The strategy is:
1. Use Security LoginController (has face login)
2. Use Expertise ExpertDashboardController (has statistics)
3. Include all unique controllers from each branch
4. Update FermeController to use expertise Ferme model

## Status Update
- [x] Analyzed LoginController differences
- [x] Identified Security version as superior
- [x] Analyzed ExpertDashboardController
- [x] Identified unique controllers per branch
- [x] Created merge strategy
