# Research Area: Navigation Patterns

## Status: 🟢 Complete

## What I Need To Learn
- How navigation is currently implemented across controllers
- Duplicated navigateWithFade() methods
- How to centralize navigation in NavigationUtil

## Files Examined
- [x] `src/main/java/tn/esprit/farmai/utils/NavigationUtil.java` — Central navigation utility
- [x] `src/main/java/tn/esprit/farmai/controllers/ExpertDashboardController.java:345-385` — Local navigateWithFade()
- [x] `src/main/java/tn/esprit/farmai/controllers/GestionAnalysesController.java:570-610` — Local navigateWithFade()
- [x] `src/main/java/tn/esprit/farmai/controllers/GestionConseilsController.java:285-330` — Local navigateWithFade()
- [x] `src/main/java/tn/esprit/farmai/controllers/MesCulturesController.java:120-160` — Local navigateWithFade()

## Findings

### NavigationUtil Central Methods

**Location:** `src/main/java/tn/esprit/farmai/utils/NavigationUtil.java`

**Existing Methods:**
```java
public static void navigateTo(Stage stage, String fxmlPath, String title)
public static void navigateToLogin(Stage stage)
public static void navigateToSignup(Stage stage)
public static void navigateToDashboard(Stage stage)
public static void navigateToUserList(Stage stage)
public static void navigateToGestionAnalyses(Stage stage)
public static void navigateToGestionConseils(Stage stage)
public static void navigateToStatistics(Stage stage)
public static void logout(Stage stage)
```

**Key Implementation (Lines 28-50):**
```java
public static void navigateTo(Stage stage, String fxmlPath, String title) {
    try {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Apply main.css if available
        String cssPath = HelloApplication.class.getResource("styles/main.css") != null
            ? HelloApplication.class.getResource("styles/main.css").toExternalForm()
            : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        stage.setScene(scene);
        stage.setTitle("FarmAI - " + title);
        stage.centerOnScreen();
        stage.show();
    } catch (IOException e) {
        showError("Navigation Error", "Could not load the requested page: " + fxmlPath);
        e.printStackTrace();
    }
}
```

**Issue:** NavigationUtil.navigateTo() does NOT have fade transition

### Duplicated navigateWithFade() Methods

**1. ExpertDashboardController (Lines 345-385):**
```java
private void navigateWithFade(String fxmlPath, String title) {
    try {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        Parent currentRoot = welcomeLabel.getScene().getRoot();

        // Fade out current scene
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            try {
                // Load new view
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent newRoot = loader.load();

                // Create scene with new root
                Scene scene = new Scene(newRoot, 1200, 800);

                // Apply CSS
                String cssPath = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css") != null
                    ? getClass().getResource("/tn/esprit/farmai/styles/dashboard.css").toExternalForm()
                    : null;
                if (cssPath != null) {
                    scene.getStylesheets().add(cssPath);
                }

                // Set initial opacity for fade in
                newRoot.setOpacity(0.0);
                stage.setScene(scene);
                stage.setTitle(title);

                // Fade in new scene
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                stage.show();
            } catch (Exception e) {
                NavigationUtil.showError("Erreur", "Impossible de charger la vue: " + e.getMessage());
                e.printStackTrace();
            }
        });

        fadeOut.play();
    } catch (Exception e) {
        NavigationUtil.showError("Erreur", "Erreur de navigation: " + e.getMessage());
        e.printStackTrace();
    }
}
```

**2. GestionAnalysesController (Lines 570-610):**
- Nearly identical implementation
- Same 200ms fade out, 250ms fade in
- Same CSS application pattern

**3. GestionConseilsController (Lines 285-330):**
- Same pattern again
- Same Duration.millis(200) and Duration.millis(250)

**4. MesCulturesController (Lines 120-160):**
- Same pattern
- Same timing and CSS application

### Code Duplication Statistics

| Controller | Lines | Duplicated From |
|------------|-------|-----------------|
| ExpertDashboardController | 345-385 | Original |
| GestionAnalysesController | 570-610 | Expert pattern |
| GestionConseilsController | 285-330 | Expert pattern |
| MesCulturesController | 120-160 | Expert pattern |

**Total Duplication:** ~160 lines of nearly identical code across 4 files

### Fade Transition Specifications

**Consistent Parameters:**
- Fade Out Duration: `Duration.millis(200)`
- Fade In Duration: `Duration.millis(250)`
- Initial Opacity: 0.0
- Final Opacity: 1.0

**CSS Application:**
```java
java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
if (cssUrl != null) {
    scene.getStylesheets().add(cssUrl.toExternalForm());
}
```

## Code Patterns Observed

**Current Pattern (Duplicated):**
```java
private void navigateWithFade(String fxmlPath, String title) {
    // 1. Get current stage and root
    // 2. Create FadeTransition for fade out (200ms)
    // 3. Set onFinished to:
    //    a. Load new FXML
    //    b. Create scene with CSS
    //    c. Set initial opacity to 0
    //    d. Set new scene
    //    e. Create FadeTransition for fade in (250ms)
    //    f. Play fade in
    // 4. Play fade out
}
```

**Proposed Pattern (Centralized in NavigationUtil):**
```java
// In NavigationUtil:
public static void navigateWithFade(Stage stage, String fxmlPath, String title) {
    // Same implementation, but reusable
}

// In controllers:
NavigationUtil.navigateWithFade(getCurrentStage(), 
    "/tn/esprit/farmai/views/expert-dashboard.fxml", 
    "FarmAI - Tableau de Bord Expert");
```

## Relevance to Implementation
**HIGH PRIORITY:**
1. Add `navigateWithFade()` method to NavigationUtil
2. Remove duplicated methods from all 4 controllers
3. Update all calls to use NavigationUtil.navigateWithFade()
4. Ensure CSS application is consistent (dashboard.css)

**Benefits:**
- Single source of truth for navigation timing
- Consistent fade duration across app
- Easier to modify animation parameters
- Reduced code duplication (~160 lines)

## Status Update
- [x] Examined NavigationUtil current methods
- [x] Documented 4 duplicated navigateWithFade() methods
- [x] Identified exact duplication (200ms/250ms pattern)
- [x] Documented CSS application pattern
- [x] Proposed centralized solution
