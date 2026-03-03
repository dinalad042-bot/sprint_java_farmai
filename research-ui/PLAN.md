# Implementation Plan - UX/UI Unification

## Overview
Achieve a professional-grade, unified UX/UI for the FarmAI platform by consolidating fragmented styles, standardizing component usage, and implementing live avatar/profile updates across all interfaces.

## Research Summary
Based on **7 research areas** investigated. Full research at:
- [01-css-architecture.md](areas/01-css-architecture.md) — Color inconsistencies between auth.css and dashboard.css
- [02-utility-classes.md](areas/02-utility-classes.md) — AlertUtils exists but not consistently used
- [03-session-profile.md](areas/03-session-profile.md) — FournisseurDashboard missing property listener
- [04-dashboard-controllers.md](areas/04-dashboard-controllers.md) — 3 of 4 dashboards have correct binding
- [05-table-list-views.md](areas/05-table-list-views.md) — AvatarUtil usage gaps
- [06-form-validation.md](areas/06-form-validation.md) — Validation logic duplicated across forms
- [07-navigation-patterns.md](areas/07-navigation-patterns.md) — navigateWithFade duplicated in 4 controllers

## Current State Analysis

### Critical Issues
1. **Missing Property Listener** (FournisseurDashboardController.java:52-75)
   - Does not listen to `currentUserProperty()` 
   - Profile edits don't reflect until page reload

2. **Manual Alert Creation** (GestionAnalysesController.java:847-861, AgricoleDashboardController.java:145-154)
   - Uses `new Alert()` instead of `AlertUtils`
   - Inconsistent styling

3. **Duplicated Navigation** (~160 lines across 4 controllers)
   - ExpertDashboardController, GestionAnalysesController, GestionConseilsController, MesCulturesController

4. **Missing ValidationUtils Class**
   - Email regex in SignupController
   - CIN regex duplicated
   - Date validation in AnalyseDialog

5. **CSS Inconsistencies**
   - auth.css: `#4CAF50` for buttons
   - dashboard.css: `#388E3C` for sidebar
   - `.primary-button` vs `.primary-btn`

## Desired End State
- All dashboards listen to currentUserProperty for live avatar updates
- All alerts use AlertUtils with consistent styling
- NavigationUtil.navigateWithFade() centralizes fade navigation
- ValidationUtils centralizes all validation logic
- CSS unified with consistent color palette

## Out of Scope
- Face recognition dark theme integration
- New feature development
- Database changes

---

## Implementation Phases

### Phase 1: Fix Missing Property Listener
**Goal:** Add currentUserProperty listener to FournisseurDashboardController
**Research basis:** [03-session-profile.md](areas/03-session-profile.md), [04-dashboard-controllers.md](areas/04-dashboard-controllers.md)

**Changes:**
| File | Line | Change |
|------|------|--------|
| `FournisseurDashboardController.java` | After 65 | Add property listener block |

**Code to Add:**
```java
// Auto-refresh sidebar when user profile changes
SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
    if (newUser != null) {
        javafx.application.Platform.runLater(() -> 
            ProfileManager.updateProfileUI(newUser, welcomeLabel, userNameLabel, 
                sidebarAvatar, sidebarAvatarText));
    }
});
```

**Success criteria:**
- [ ] Profile edits update sidebar avatar immediately in Fournisseur dashboard

**Depends on:** Nothing

---

### Phase 2: Replace Manual Alerts with AlertUtils
**Goal:** Migrate all manual Alert creation to AlertUtils
**Research basis:** [02-utility-classes.md](areas/02-utility-classes.md)

**Changes:**
| File | Lines | Change |
|------|-------|--------|
| `GestionAnalysesController.java` | 847-853 | Replace `showInfo()` with `AlertUtils.showInfo()` |
| `GestionAnalysesController.java` | 855-861 | Replace `showError()` with `AlertUtils.showError()` |
| `GestionAnalysesController.java` | 863-869 | Replace `showAlert()` with `AlertUtils.showWarning()` |
| `AgricoleDashboardController.java` | 145-154 | Replace manual Alert with `AlertUtils.showInfo()` |

**Success criteria:**
- [ ] All alerts in GestionAnalyses use AlertUtils
- [ ] All alerts in AgricoleDashboard use AlertUtils

**Depends on:** Phase 1

---

### Phase 3: Centralize Navigation with navigateWithFade
**Goal:** Add navigateWithFade to NavigationUtil, remove duplicates
**Research basis:** [07-navigation-patterns.md](areas/07-navigation-patterns.md)

**Changes:**
| File | Change |
|------|--------|
| `NavigationUtil.java` | Add `navigateWithFade(Stage, String, String)` method |
| `ExpertDashboardController.java` | Remove local `navigateWithFade()`, use NavigationUtil |
| `GestionAnalysesController.java` | Remove local `navigateWithFade()`, use NavigationUtil |
| `GestionConseilsController.java` | Remove local `navigateWithFade()`, use NavigationUtil |
| `MesCulturesController.java` | Remove local `navigateWithFade()`, use NavigationUtil |

**NavigationUtil Method to Add:**
```java
public static void navigateWithFade(Stage stage, String fxmlPath, String title) {
    try {
        Parent currentRoot = stage.getScene().getRoot();
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
                Parent newRoot = loader.load();
                Scene scene = new Scene(newRoot, 1200, 800);
                
                URL cssUrl = NavigationUtil.class.getResource("/tn/esprit/farmai/styles/dashboard.css");
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
                
                newRoot.setOpacity(0.0);
                stage.setScene(scene);
                stage.setTitle(title);
                
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            } catch (Exception e) {
                showError("Navigation Error", e.getMessage());
            }
        });
        fadeOut.play();
    } catch (Exception e) {
        showError("Navigation Error", e.getMessage());
    }
}
```

**Success criteria:**
- [ ] NavigationUtil has navigateWithFade method
- [ ] ~160 lines of duplicated code removed
- [ ] Fade transitions still work in all 4 controllers

**Depends on:** Phase 2

---

### Phase 4: Create ValidationUtils
**Goal:** Centralize validation logic
**Research basis:** [06-form-validation.md](areas/06-form-validation.md)

**Changes:**
| File | Change |
|------|--------|
| **NEW** `ValidationUtils.java` | Create utility class |

**ValidationUtils API:**
```java
public class ValidationUtils {
    public static boolean isValidEmail(String email);
    public static boolean isValidCin(String cin);  // 8 digits
    public static boolean isValidPhone(String phone);
    public static boolean isValidDate(LocalDate date, int minYear, int maxYear);
    public static void setupRealTimeEmailValidation(TextField field);
    public static void setupRealTimeCinValidation(TextField field);
    public static void showFieldError(TextField field);
    public static void clearFieldError(TextField field);
}
```

**Success criteria:**
- [ ] ValidationUtils class created
- [ ] SignupController uses ValidationUtils
- [ ] AnalyseDialog uses ValidationUtils
- [ ] AjoutConseilController uses ValidationUtils

**Depends on:** Phase 3

---

### Phase 5: Unify CSS Architecture
**Goal:** Consolidate auth.css and dashboard.css, standardize colors
**Research basis:** [01-css-architecture.md](areas/01-css-architecture.md)

**Changes:**
| File | Change |
|------|--------|
| **NEW** `root.css` | CSS variables for theming |
| `main.css` | Merge auth.css + dashboard.css |

**CSS Variables (root.css):**
```css
.root {
    /* Primary Green - Standardizing on #2E7D32 */
    -fx-primary-green: #2E7D32;
    -fx-primary-green-light: #4CAF50;
    -fx-primary-green-dark: #1B5E20;
    
    /* Backgrounds */
    -fx-bg-surface: #F4F6F8;
    -fx-bg-dark: #263238;
    -fx-bg-input: #37474F;
    
    /* Text */
    -fx-text-primary: #263238;
    -fx-text-secondary: #546E7A;
    -fx-text-light: #FFFFFF;
    
    /* Status */
    -fx-error: #D32F2F;
    -fx-success: #2E7D32;
    -fx-warning: #FF9800;
    
    /* Inputs */
    -fx-input-radius: 8px;
    -fx-input-padding: 12px;
}
```

**Success criteria:**
- [ ] root.css created with CSS variables
- [ ] main.css consolidates both files
- [ ] All greens use consistent #2E7D32
- [ ] Button classes standardized to `-btn` suffix

**Depends on:** Phase 4

---

## Testing Strategy

### Automated Testing
```bash
# Compile after each phase
mvn clean compile

# Run existing tests
mvn test
```

### Manual Testing Checklist
- [ ] Edit profile picture → Verify all open dashboards update immediately
- [ ] Navigate between views → Verify 200ms/250ms fade transitions
- [ ] Submit invalid form → Verify red border appears on focus loss
- [ ] Trigger alert → Verify consistent AlertUtils styling
- [ ] View tables → Verify text is visible and properly formatted

## Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| FournisseurDashboard listener breaks existing flow | Low | Medium | Test profile edit thoroughly |
| NavigationUtil.navigateWithFade introduces bugs | Low | High | Test all navigation paths |
| CSS consolidation breaks existing styles | Medium | High | Keep backup, test all views |
| ValidationUtils changes validation behavior | Low | High | Verify all validation rules |

## All Files Referenced

### CSS Files
- `src/main/resources/tn/esprit/farmai/styles/auth.css`
- `src/main/resources/tn/esprit/farmai/styles/dashboard.css`

### Utility Classes
- `src/main/java/tn/esprit/farmai/utils/AvatarUtil.java`
- `src/main/java/tn/esprit/farmai/utils/AlertUtils.java`
- `src/main/java/tn/esprit/farmai/utils/NavigationUtil.java`
- `src/main/java/tn/esprit/farmai/utils/ProfileManager.java`
- `src/main/java/tn/esprit/farmai/utils/SessionManager.java`

### Controllers to Modify
- `src/main/java/tn/esprit/farmai/controllers/FournisseurDashboardController.java`
- `src/main/java/tn/esprit/farmai/controllers/GestionAnalysesController.java`
- `src/main/java/tn/esprit/farmai/controllers/AgricoleDashboardController.java`
- `src/main/java/tn/esprit/farmai/controllers/ExpertDashboardController.java`
- `src/main/java/tn/esprit/farmai/controllers/GestionConseilsController.java`
- `src/main/java/tn/esprit/farmai/controllers/MesCulturesController.java`

### New Files to Create
- `src/main/java/tn/esprit/farmai/utils/ValidationUtils.java`
- `src/main/resources/tn/esprit/farmai/styles/root.css`
- `src/main/resources/tn/esprit/farmai/styles/main.css`
