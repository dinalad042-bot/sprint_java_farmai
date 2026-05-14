# Research Area: Dashboard Controllers

## Status: 🟢 Complete

## What I Need To Learn
- Common patterns across all dashboard controllers
- Which controllers have live update binding
- Consistency in sidebar structure and initialization

## Files Examined
- [x] `src/main/java/tn/esprit/farmai/controllers/AdminDashboardController.java` — Full implementation
- [x] `src/main/java/tn/esprit/farmai/controllers/ExpertDashboardController.java` — Full implementation
- [x] `src/main/java/tn/esprit/farmai/controllers/AgricoleDashboardController.java` — Full implementation
- [x] `src/main/java/tn/esprit/farmai/controllers/FournisseurDashboardController.java` — Missing listener

## Findings

### Common FXML Elements Across Dashboards

All 4 dashboards have these sidebar elements:
```java
@FXML private Label welcomeLabel;
@FXML private Label userNameLabel;
@FXML private Label userRoleLabel;
@FXML private Circle sidebarAvatar;
@FXML private Text sidebarAvatarText;
```

**Verified Presence:**
- ✅ `AdminDashboardController.java:25-44` — All elements present
- ✅ `ExpertDashboardController.java:28-45` — All elements present
- ✅ `AgricoleDashboardController.java:31-48` — All elements present
- ✅ `FournisseurDashboardController.java:27-42` — All elements present

### initialize() Method Comparison

**AdminDashboardController.java:69-102:**
```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    // Set user info
    User currentUser = SessionManager.getInstance().getCurrentUser();
    if (currentUser != null) {
        ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, 
            sidebarAvatar, sidebarAvatarText);
        if (userRoleLabel != null) {
            userRoleLabel.setText(currentUser.getRole().getDisplayName());
        }
    }

    // Auto-refresh sidebar when user profile changes
    SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
        if (newUser != null) {
            Platform.runLater(() -> ProfileManager.updateProfileUI(newUser, welcomeLabel, 
                userNameLabel, sidebarAvatar, sidebarAvatarText));
        }
    });

    // Load statistics
    loadStatistics();
}
```

**ExpertDashboardController.java:62-87:**
```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    User currentUser = SessionManager.getInstance().getCurrentUser();
    if (currentUser != null) {
        ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, 
            sidebarAvatar, sidebarAvatarText);
        if (userRoleLabel != null) {
            userRoleLabel.setText(currentUser.getRole().getDisplayName());
        }
    }

    // Auto-refresh sidebar when user profile changes
    SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
        if (newUser != null) {
            Platform.runLater(() -> ProfileManager.updateProfileUI(newUser, welcomeLabel, 
                userNameLabel, sidebarAvatar, sidebarAvatarText));
        }
    });

    loadStatistics();
}
```

**AgricoleDashboardController.java:75-110:**
```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    User currentUser = SessionManager.getInstance().getCurrentUser();
    if (currentUser != null) {
        ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, 
            sidebarAvatar, sidebarAvatarText);
        if (userRoleLabel != null) {
            userRoleLabel.setText(currentUser.getRole().getDisplayName());
        }
    }

    // Auto-refresh sidebar when user profile changes
    SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
        if (newUser != null) {
            Platform.runLater(() -> ProfileManager.updateProfileUI(newUser, welcomeLabel, 
                userNameLabel, sidebarAvatar, sidebarAvatarText));
        }
    });

    loadStatistics();
    checkNotifications();  // Unique to Agricole
}
```

**FournisseurDashboardController.java:52-75:**
```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    User currentUser = SessionManager.getInstance().getCurrentUser();
    if (currentUser != null) {
        ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, 
            sidebarAvatar, sidebarAvatarText);
        if (userRoleLabel != null) {
            userRoleLabel.setText(currentUser.getRole().getDisplayName());
        }
    }

    // ❌ MISSING: No currentUserProperty listener!
    
    loadStatistics();
}
```

### Navigation Patterns

**ExpertDashboardController has local navigateWithFade():**
```java
private void navigateWithFade(String fxmlPath, String title) { ... }  // Lines 345-385
```

**Called from:**
- `handleConsultations()` → gestion-analyses.fxml
- `handleRecommendations()` → gestion-conseils.fxml
- `handleStatistics()` → statistics.fxml

**AgricoleDashboardController has local fade navigation:**
```java
// handleAIAnalysis() method contains inline fade transition (lines 230-280)
```

**AdminDashboardController uses direct navigation:**
```java
@FXML
private void handleUserLogs() {
    // Direct FXML loading without fade (lines 156-170)
}
```

### Statistics Loading Pattern

All dashboards follow same pattern:
```java
private void loadStatistics() {
    try {
        // Service calls
        int count = service.selectALL().size();
        if (label != null) label.setText(String.valueOf(count));
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error", e);
        if (label != null) label.setText("-");
    }
}
```

### Role-Based Sidebar Styling

**UserListController.java:255-268** has role styling:
```java
private String getRoleStyle(Role role) {
    switch (role) {
        case ADMIN:    return "-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F;";
        case EXPERT:   return "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2;";
        case AGRICOLE: return "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32;";
        case FOURNISSEUR: return "-fx-background-color: #FFF3E0; -fx-text-fill: #EF6C00;";
    }
}
```

**This should be applied to dashboard role labels for consistency**

## Code Patterns Observed

**Consistent Pattern (4 dashboards):**
1. Inject sidebar FXML elements
2. Call `ProfileManager.updateProfileUI()` in initialize()
3. Call `loadStatistics()` 
4. Handle logout via `NavigationUtil.logout(stage)`
5. Handle profile via `ProfileManager.showProfileEditDialog()`

**Inconsistent Pattern:**
- Only 3 of 4 dashboards have `currentUserProperty` listener
- Navigation style varies (fade vs direct)
- Agricole has unique notification badge handling

## Relevance to Implementation
**CRITICAL:**
1. FournisseurDashboardController MUST add missing currentUserProperty listener
2. Consider standardizing navigation (fade vs direct) across all dashboards
3. Role labels should use consistent styling from UserListController pattern
4. All dashboards should handle profile edit the same way

## Status Update
- [x] Compared all 4 dashboard controllers
- [x] Documented common FXML elements
- [x] Identified missing listener in FournisseurDashboard
- [x] Documented navigation inconsistencies
- [x] Documented role styling pattern
