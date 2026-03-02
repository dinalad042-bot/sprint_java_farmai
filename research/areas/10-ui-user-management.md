# Research Area: UI Improvements - User Management

## Status: 🟢 Complete

## What I Need To Learn
1. Where is the User Log button located?
2. How does user edit notification work?
3. How to add UserLogService integration for user edits?

## Files Examined
- [x] `src/main/resources/tn/esprit/farmai/views/admin-dashboard.fxml` — Found "📋 Logs Audit" button
- [x] `src/main/resources/tn/esprit/farmai/views/user-list.fxml` — No User Log button here
- [x] `src/main/java/tn/esprit/farmai/controllers/UserListController.java` — Has notification but no UserLogService

## Findings

### User Log Button Location
- **Location**: `admin-dashboard.fxml:52`
- **Content**: `<Button text="📋  Logs Audit" styleClass="nav-button" onAction="#handleUserLogs" maxWidth="Infinity"/>`
- **Action**: Need to REMOVE this button

### User Edit Notification
- **Location**: `UserListController.java:264-279`
- **Current behavior**: Uses `NotificationManager.addNotification()` but NOT `UserLogService`
- **Missing**: No logging to user_log table when editing users
- **Fix needed**: Add UserLogService integration

### Signup Admin Prevention
- **Location**: `SignupController.java:39`
- **Current**: `roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));`
- **Issue**: Shows ALL roles including ADMIN
- **Fix**: Filter out ADMIN role from signup options

## Code Patterns Observed
- Pattern: UserLogService used in `UserService.java` for login/logout — should be used for user edits too

## Relevance to Implementation
1. Remove "📋 Logs Audit" button from admin dashboard sidebar
2. Add UserLogService to UserListController for edit operations
3. Filter ADMIN from signup role ComboBox

## Status Update
- [x] Initial investigation
- [x] Found User Log button in admin-dashboard.fxml
- [x] Found signup shows ADMIN role
- [x] Found UserListController lacks UserLogService integration