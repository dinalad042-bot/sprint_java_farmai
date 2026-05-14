# Research Area: Session & Profile Management

## Status: 🟢 Complete

## What I Need To Learn
- How SessionManager manages user state
- How profile updates propagate to UI
- Which controllers properly implement live update binding

## Files Examined
- [x] `src/main/java/tn/esprit/farmai/utils/SessionManager.java:1-96` — Core session management
- [x] `src/main/java/tn/esprit/farmai/utils/ProfileManager.java:1-268` — Profile UI updates
- [x] `src/main/java/tn/esprit/farmai/controllers/AdminDashboardController.java:1-200` — Has property listener
- [x] `src/main/java/tn/esprit/farmai/controllers/ExpertDashboardController.java:1-150` — Has property listener
- [x] `src/main/java/tn/esprit/farmai/controllers/AgricoleDashboardController.java:1-300` — Has property listener
- [x] `src/main/java/tn/esprit/farmai/controllers/FournisseurDashboardController.java:1-130` — MISSING listener
- [x] `src/main/java/tn/esprit/farmai/controllers/UserListController.java:1-400` — Has property listener

## Findings

### SessionManager Architecture

**Observable Property Pattern (Lines 17-22):**
```java
private final ObjectProperty<User> currentUserProperty = new SimpleObjectProperty<>(null);

public ObjectProperty<User> currentUserProperty() {
    return currentUserProperty;
}
```

**State Update (Lines 33-37):**
```java
public void setCurrentUser(User user) {
    this.currentUser = user;
    this.currentUserProperty.set(user);  // Notifies all listeners
}
```

**Benefits:**
- JavaFX binding enables automatic UI updates
- No manual refresh needed across controllers
- Works across different windows/stages

### ProfileManager Live Update Flow

**Update Trigger Chain:**
1. User edits profile via `ProfileManager.showProfileEditDialog()`
2. On save: `userService.updateOne(updatedUser)` → Database updated
3. `SessionManager.getInstance().setCurrentUser(updatedUser)` → Property updated
4. All registered listeners receive `(obs, oldUser, newUser)` callback
5. Listeners call `ProfileManager.updateProfileUI()` with new user data

### Controller Implementation Analysis

**Correct Implementation (AdminDashboardController.java:69-91):**
```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    // Initial setup
    User currentUser = SessionManager.getInstance().getCurrentUser();
    if (currentUser != null) {
        ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, 
            sidebarAvatar, sidebarAvatarText);
    }

    // CRITICAL: Live update binding
    SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
        if (newUser != null) {
            Platform.runLater(() -> ProfileManager.updateProfileUI(newUser, welcomeLabel, 
                userNameLabel, sidebarAvatar, sidebarAvatarText));
        }
    });
    
    loadStatistics();
}
```

**MISSING Implementation (FournisseurDashboardController.java:52-75):**
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
    // Profile edits won't reflect until page reload
    
    loadStatistics();
}
```

### ProfileManager.updateProfileUI Deep Dive

**Method Signature (ProfileManager.java:214-228):**
```java
public static void updateProfileUI(User user, Label welcomeLabel, Label userNameLabel, 
    Circle avatarCircle, Text avatarText)
```

**What It Does:**
1. Updates `welcomeLabel` with "Bienvenue, [FullName]!"
2. Updates `userNameLabel` with `[FullName]`
3. Calls `AvatarUtil.loadUserImageIntoCircle()` for the avatar
4. Toggles `avatarText` visibility based on image load success

**Avatar Loading Chain:**
```
ProfileManager.updateProfileUI()
  → AvatarUtil.loadUserImageIntoCircle()
    → Sets immediate color fill
    → Tries FileInputStream for local files
    → Falls back to HTTP URL loading
    → Falls back to ui-avatars.com API
```

### UserListController Special Pattern

**Unique Implementation (UserListController.java:85-95):**
```java
SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
    if (newUser != null) {
        Platform.runLater(() -> {
            updateUserSessionUI();
            userListView.refresh(); // Force ListView cells to re-render
        });
    }
});
```

**Key Difference:** Also refreshes ListView cells to update avatars in the user list

## Code Patterns Observed

**Standard Pattern (4 controllers):**
```java
// 1. Initial UI setup
ProfileManager.updateProfileUI(currentUser, ...);

// 2. Live binding
SessionManager.getInstance().currentUserProperty().addListener((obs, old, new) -> {
    if (new != null) Platform.runLater(() -> ProfileManager.updateProfileUI(new, ...));
});
```

**FXML Requirements for Pattern:**
```xml
<!-- Sidebar must have these nodes -->
<Label fx:id="welcomeLabel" />
<Label fx:id="userNameLabel" />
<Circle fx:id="sidebarAvatar" />
<Text fx:id="sidebarAvatarText" />
```

## Relevance to Implementation
**CRITICAL:**
1. FournisseurDashboardController MUST add the property listener
2. All controllers should follow the same 2-step pattern (init + listener)
3. ListView-based controllers (UserList) should also refresh their lists
4. FXML files must have the required node IDs for ProfileManager to work

## Status Update
- [x] Analyzed SessionManager observable pattern
- [x] Documented correct implementation in 4 controllers
- [x] Identified missing listener in FournisseurDashboardController
- [x] Documented ProfileManager update chain
- [x] Documented UserListController refresh pattern
