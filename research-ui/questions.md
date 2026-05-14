# Open Questions

## Answerable by Code (Need More Research)
- [ ] Do all FXML files have the required sidebar nodes (sidebarAvatar, sidebarAvatarText)? → assigned to Area 08
- [ ] Which controllers use manual Alert instead of AlertUtils? → Need to search all controllers
- [ ] Are there any other duplicated utility methods across controllers? → Need full codebase scan

## Need Human Input
- [ ] ⚠️ Should dark theme (#1a1a2e from face-recognition) be integrated as a theme variant?
- [ ] ⚠️ Which green should be the standard primary: #2E7D32 or #388E3C?
- [ ] ⚠️ Should GestionAnalyses table display technician avatars or just analysis images?

## Resolved
- [x] **Q: Does SessionManager support live updates?** 
  - **Answer:** Yes, via `currentUserProperty()` Observable
  - **Found in:** `SessionManager.java:17-22`
  - **Area:** 03 - Session & Profile Management

- [x] **Q: Why do some avatars not update when profile is edited?**
  - **Answer:** Controllers missing `currentUserProperty.addListener()`
  - **Found in:** `FournisseurDashboardController.java:52-75` missing listener
  - **Area:** 04 - Dashboard Controllers

- [x] **Q: Is there a centralized alert utility?**
  - **Answer:** Yes, `AlertUtils` with standardized styling
  - **Found in:** `AlertUtils.java:1-244`
  - **Area:** 02 - Utility Classes

- [x] **Q: Are table visibility issues already fixed?**
  - **Answer:** Yes, dashboard.css has comprehensive fixes
  - **Found in:** `dashboard.css:119-165`
  - **Area:** 01 - CSS Architecture

- [x] **Q: Is there a ValidationUtils class?**
  - **Answer:** No - validation logic is duplicated across controllers
  - **Found in:** Search returned 0 results for ValidationUtils
  - **Area:** 06 - Form Validation

- [x] **Q: Does NavigationUtil have fade navigation?**
  - **Answer:** No - it's duplicated in 4 controllers
  - **Found in:** Expert, GestionAnalyses, GestionConseils, MesCultures controllers
  - **Area:** 07 - Navigation Patterns
