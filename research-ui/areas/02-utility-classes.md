# Research Area: Utility Classes

## Status: 🟢 Complete

## What I Need To Learn
- Which utility classes exist and their capabilities
- How consistently they're used across controllers
- Missing utility classes that should be created

## Files Examined
- [x] `src/main/java/tn/esprit/farmai/utils/AvatarUtil.java` — Comprehensive avatar handling
- [x] `src/main/java/tn/esprit/farmai/utils/AlertUtils.java` — Standardized alert dialogs
- [x] `src/main/java/tn/esprit/farmai/utils/NavigationUtil.java` — Navigation helpers
- [x] `src/main/java/tn/esprit/farmai/utils/ProfileManager.java` — Profile editing and UI updates
- [x] `src/main/java/tn/esprit/farmai/utils/SessionManager.java` — Session management with observable properties

## Findings

### AvatarUtil (Lines 1-315)
**Location:** `src/main/java/tn/esprit/farmai/utils/AvatarUtil.java`

**Public API Methods:**
1. `createCircularAvatar(User user, double size)` — Returns StackPane with initials fallback
2. `createCircularImageView(User user, double size)` — Returns ImageView with circular clip
3. `loadUserImageIntoCircle(Circle circle, User user)` — Loads into existing FXML Circle
4. `loadUserImageIntoImageView(ImageView imageView, User user, double size)` — Loads into existing ImageView
5. `applyCircularClip(ImageView imageView, double size)` — Applies permanent circular clip

**Key Features:**
- Uses `FileInputStream` for local files (bypasses JavaFX URL cache)
- Shows colored initials immediately while image loads
- Uses ui-avatars.com API as fallback
- Handles both HTTP URLs and local file paths
- Applies cache busters for HTTP URLs

**Usage in Codebase:**
- ✅ `UserListController.java:135` — `AvatarUtil.createCircularAvatar(user, 50)`
- ✅ `UserListController.java:178` — `AvatarUtil.loadUserImageIntoImageView(profileImageView, currentUser, 40)`
- ✅ `ProfileManager.java:217` — Delegates to `AvatarUtil.loadUserImageIntoCircle()`

**MISSING Usage:**
- ❌ `GestionAnalysesController.java:366-393` — Uses manual ImageView creation instead of AvatarUtil

### AlertUtils (Lines 1-244)
**Location:** `src/main/java/tn/esprit/farmai/utils/AlertUtils.java`

**Public API Methods:**
- `showError(String title, String message)` — Error alerts with `-fx-accent: #D32F2F`
- `showSuccess(String title, String message)` — Success alerts with `-fx-accent: #2E7D32`
- `showWarning(String title, String message)` — Warning alerts with `-fx-accent: #FF9800`
- `showInfo(String title, String message)` — Info alerts
- `showConfirmation(...)` — Confirmation dialogs with custom buttons
- `showExpandable(...)` — Alerts with expandable text areas
- `showValidationErrors(List<String> errors)` — Formatted validation error display
- `showToast(String message, int durationMs)` — Auto-dismiss notifications

**CSS Styling Applied:**
```java
private static final String ERROR_STYLE = "-fx-accent: #D32F2F;";
private static final String SUCCESS_STYLE = "-fx-accent: #2E7D32;";
private static final String WARNING_STYLE = "-fx-accent: #FF9800;";
private static final String INFO_STYLE = "-fx-accent: #1976D2;";
```

**Usage in Codebase:**
- ✅ `AnalyseDialog.java:147` — `AlertUtils.showWarning()`
- ✅ `AnalyseDialog.java:165` — `AlertUtils.showSuccess()`
- ✅ `AnalyseDialog.java:169` — `AlertUtils.showError()`
- ✅ `GestionConseilsController.java:287` — `AlertUtils.showWarning()`

**MISSING Usage:**
- ❌ `GestionAnalysesController.java:847-853` — Uses manual `new Alert(Alert.AlertType.INFORMATION)`
- ❌ `GestionAnalysesController.java:855-861` — Uses manual `new Alert(Alert.AlertType.ERROR)`
- ❌ `AgricoleDashboardController.java:145-154` — Uses manual `new Alert(Alert.AlertType.INFORMATION)`

### NavigationUtil (Lines 1-172)
**Location:** `src/main/java/tn/esprit/farmai/utils/NavigationUtil.java`

**Public API Methods:**
- `navigateTo(Stage, String fxmlPath, String title)` — Generic navigation
- `navigateToLogin(Stage)` — Login view
- `navigateToSignup(Stage)` — Signup view
- `navigateToDashboard(Stage)` — Role-based dashboard routing
- `navigateToUserList(Stage)` — User management
- `navigateToGestionAnalyses(Stage)` — Analysis management
- `navigateToGestionConseils(Stage)` — Advice management
- `navigateToStatistics(Stage)` — Statistics view
- `logout(Stage)` — Logout with confirmation
- `showError/showSuccess/showConfirmation/showWarning/showInfo` — Alert wrappers

**Key Feature:** Applies dashboard.css automatically to dashboard views (lines 102-106)

**Usage in Codebase:**
- ✅ Used consistently across all controllers for navigation

### ProfileManager (Lines 1-268)
**Location:** `src/main/java/tn/esprit/farmai/utils/ProfileManager.java`

**Public API Methods:**
- `showProfileEditDialog(Window owner)` — Shows profile edit dialog with image upload
- `updateProfileUI(User user, Label welcomeLabel, Label userNameLabel, Circle avatarCircle, Text avatarText)` — Updates sidebar UI
- `loadUserImageIntoCircle(Circle circle, User user)` — Delegates to AvatarUtil
- `loadUserImageIntoImageView(ImageView imageView, User user)` — Delegates to AvatarUtil

**Key Features:**
- Uses GridPane with hgap=10, vgap=10, padding=20px (standard form layout)
- Integrates with SessionManager for live updates
- Uses dashboard.css for dialog styling (line 44)

**Usage in Codebase:**
- ✅ All dashboard controllers call `ProfileManager.updateProfileUI()` in initialize()
- ✅ All dashboard controllers call `ProfileManager.showProfileEditDialog()` on profile click

### SessionManager (Lines 1-96)
**Location:** `src/main/java/tn/esprit/farmai/utils/SessionManager.java`

**Key Feature:** Observable property for live UI updates
```java
private final ObjectProperty<User> currentUserProperty = new SimpleObjectProperty<>(null);

public ObjectProperty<User> currentUserProperty() {
    return currentUserProperty;
}
```

**Usage Pattern (Correct):**
```java
// In initialize() method:
SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
    if (newUser != null) {
        Platform.runLater(() -> ProfileManager.updateProfileUI(newUser, ...));
    }
});
```

**Controllers with Property Listener:**
- ✅ `AdminDashboardController.java:85-91`
- ✅ `ExpertDashboardController.java:77-83`
- ✅ `AgricoleDashboardController.java:88-94`

**Controllers MISSING Property Listener:**
- ❌ `FournisseurDashboardController.java` — Only calls updateProfileUI once, no listener

## Code Patterns Observed
1. **Consistent Pattern:** Controllers that listen to currentUserProperty have live avatar updates
2. **Inconsistent Pattern:** Some controllers still create manual Alerts instead of using AlertUtils
3. **Missing Pattern:** No ValidationUtils class exists for email/CIN validation

## Relevance to Implementation
**CRITICAL:** 
1. All controllers must use AlertUtils instead of manual Alert creation
2. GestionAnalysesController must use AvatarUtil for table cell avatars
3. FournisseurDashboardController needs currentUserProperty listener added
4. Need to create ValidationUtils for centralized validation logic

## New Questions Generated
- Should NavigationUtil also have a standardized `navigateWithFade` method? → Yes, already has it partially

## Status Update
- [x] Examined all utility classes
- [x] Documented proper usage patterns
- [x] Identified missing usage locations
- [x] Documented CSS styling in AlertUtils
