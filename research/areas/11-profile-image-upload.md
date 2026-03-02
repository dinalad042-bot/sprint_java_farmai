# Research Area: Profile Edit - Image Upload

## Status: 🟢 Complete

## What I Need To Learn
- How ProfileManager.showProfileEditDialog() currently works
- How to add image upload functionality similar to UserListController
- Ensure the image path is saved to the user record

## Files Examined
- [x] `src/main/java/tn/esprit/farmai/utils/ProfileManager.java:35-120` — showProfileEditDialog() method lacks image upload
- [x] `src/main/java/tn/esprit/farmai/controllers/UserListController.java:350-400` — Has working image upload example

## Findings

### Current State (ProfileManager.java)
**Location**: `src/main/java/tn/esprit/farmai/utils/ProfileManager.java:35-120`
**What it does**: Creates a dialog with form fields for nom, prenom, email, cin, telephone, adresse, password
**What's missing**: No image upload button or image preview

### Working Example (UserListController.java)
**Location**: `src/main/java/tn/esprit/farmai/controllers/UserListController.java:350-400`
**Pattern used**:
```java
ImageView imagePreview = new ImageView();
imagePreview.setFitWidth(80);
imagePreview.setFitHeight(80);

Button uploadImageBtn = new Button("Choisir Photo");
final StringBuilder selectedImagePath = new StringBuilder();

uploadImageBtn.setOnAction(e -> {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Choisir une image");
    fileChooser.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
    File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
    if (selectedFile != null) {
        selectedImagePath.setLength(0);
        selectedImagePath.append(selectedFile.getAbsolutePath());
        imagePreview.setImage(new Image(selectedFile.toURI().toString()));
    }
});
```

## Code Patterns Observed
- Use FileChooser with ExtensionFilter for image files
- Use StringBuilder to capture the selected path
- Update ImageView preview when image is selected
- Save path to user.setImageUrl() when saving

## Relevance to Implementation
Users need to be able to update their profile picture from the profile edit dialog. Currently they can only do this from the admin user management screen.

## Implementation Required
1. Add ImageView for preview in ProfileManager.showProfileEditDialog()
2. Add "Choisir Photo" button with FileChooser
3. Load current user image if exists
4. Save selected image path when user is updated
5. Update SessionManager with new user data including image

## Status Update
- [x] Analyzed ProfileManager current implementation
- [x] Found working pattern in UserListController
- [ ] Implement image upload in ProfileManager
