# Research Area: Table & List Views

## Status: 🟢 Complete

## What I Need To Learn
- How tables currently display avatars/images
- Whether AvatarUtil is used in table cells
- Table visibility fixes already applied

## Files Examined
- [x] `src/main/java/tn/esprit/farmai/controllers/GestionAnalysesController.java:366-393` — Image column cell factory
- [x] `src/main/java/tn/esprit/farmai/controllers/GestionConseilsController.java:85-140` — Actions column
- [x] `src/main/java/tn/esprit/farmai/controllers/UserListController.java:110-180` — Uses AvatarUtil correctly
- [x] `src/main/resources/tn/esprit/farmai/styles/dashboard.css:119-165` — Table styling

## Findings

### GestionAnalysesController Image Column (Lines 366-393)

**Current Implementation (MANUAL):**
```java
colImage.setCellFactory(col -> new TableCell<Analyse, String>() {
    @Override
    protected void updateItem(String imageUrl, boolean empty) {
        super.updateItem(imageUrl, empty);
        if (empty) {
            setGraphic(null);
        } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(40);
                imageView.setFitHeight(30);
                imageView.setPreserveRatio(true);
                
                File imageFile = new File(imageUrl);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                } else {
                    imageView.setImage(new Image("file:" + imageUrl, true));
                }
                
                setGraphic(imageView);
            } catch (Exception e) {
                Label label = new Label("[IMG]");
                label.setStyle("-fx-text-fill: #4CAF50; ...");
                setGraphic(label);
            }
        } else {
            Label label = new Label("[NO IMG]");
            label.setStyle("-fx-text-fill: #9E9E9E; ...");
            setGraphic(label);
        }
    }
});
```

**Issues:**
1. ❌ Creates manual `ImageView` instead of using `AvatarUtil`
2. ❌ No circular clipping applied
3. ❌ No fallback to user initials
4. ❌ Generic `[IMG]` / `[NO IMG]` placeholders instead of avatar

### UserListController Correct Implementation (Lines 110-180)

**Avatar Usage:**
```java
private void setupListView() {
    userListView.setCellFactory(param -> new ListCell<User>() {
        @Override
        protected void updateItem(User user, boolean empty) {
            // ...
            // Create circular avatar using AvatarUtil
            StackPane avatar = AvatarUtil.createCircularAvatar(user, 50);
            // ...
        }
    });
}
```

**Benefits:**
- ✅ Uses `AvatarUtil.createCircularAvatar()` with initials fallback
- ✅ Consistent sizing and styling
- ✅ Automatic image loading with fallback chain

### GestionConseilsController Actions Column (Lines 116-165)

**Current Implementation:**
```java
colActions.setCellFactory(param -> new TableCell<Conseil, Void>() {
    private final Button editBtn = new Button("✎");
    private final Button deleteBtn = new Button("🗑");
    private final Button ttsBtn = new Button("🔊");
    // ...
});
```

**Note:** This is for actions, not avatars. But if user avatars were needed, should use AvatarUtil.

### Table Visibility CSS (Already Fixed)

**dashboard.css Lines 119-165:**
```css
.table-row-cell {
    -fx-background-color: white;
    -fx-border-color: #F5F5F5;
    -fx-border-width: 0 0 1px 0;
    -fx-padding: 8px 0;
    -fx-min-height: 45px;
    -fx-pref-height: 55px;
    -fx-cell-size: 55px;
}

.table-cell {
    -fx-padding: 10px 12px;
    -fx-text-fill: #000000;
    -fx-alignment: CENTER_LEFT;
    -fx-valignment: CENTER;
}

/* Force visible text in all table cells */
.table-row-cell .table-cell {
    -fx-text-fill: black !important;
}
```

**Status:** Table visibility issues are already addressed in CSS

### UserLogController ListView Pattern (Lines 55-95)

**Cell Factory:**
```java
logListView.setCellFactory(param -> new ListCell<UserLog>() {
    @Override
    protected void updateItem(UserLog log, boolean empty) {
        // Creates card-style HBox layout
        // Uses color-coded action badges
    }
});
```

**Pattern for Table/Cell Avatars:**

If technician/user avatars were needed in GestionAnalyses table:

```java
// Get User for the analyse
User technician = userService.getById(analyse.getIdTechnicien());
// Use AvatarUtil
StackPane avatar = AvatarUtil.createCircularAvatar(technician, 30);
setGraphic(avatar);
```

## Code Patterns Observed

**Correct Pattern (UserListController):**
- Use `AvatarUtil.createCircularAvatar(user, size)` for any user avatar
- Works in ListView cells and TableView cells

**Incorrect Pattern (GestionAnalysesController):**
- Manual ImageView creation
- No fallback handling
- No circular clipping

## Relevance to Implementation
**HIGH PRIORITY:**
1. GestionAnalysesController image column should use AvatarUtil if displaying user avatars
2. Any table showing users should use AvatarUtil for consistent avatar display
3. Table visibility CSS is already fixed — no changes needed there
4. For technician/farmer avatars in analyses table, would need to fetch User and call AvatarUtil

## Status Update
- [x] Examined GestionAnalysesController image column
- [x] Examined UserListController correct AvatarUtil usage
- [x] Verified table visibility CSS fixes present
- [x] Documented correct/incorrect patterns
