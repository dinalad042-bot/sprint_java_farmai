# Open Questions

## Answerable by Code (Need More Research)
*All resolved*

## Need Human Input
*All resolved*

## Resolved

### Q1: Where is captcha implemented in securite-aymen?
- [x] **RESOLVED** - Commit `ad2b90a` - Login form only
- **Location**: `LoginController.java` and new `CaptchaUtil.java`
- **Area**: 02 - Captcha Feature

### Q2: What type of captcha is used?
- [x] **RESOLVED** - Image-based alphanumeric captcha
- **Details**: 5-6 character random string, distorted image with noise
- **Area**: 02 - Captcha Feature

### Q3: Where is excel export implemented?
- [x] **RESOLVED** - Commit `bb95d02` - User list only
- **Location**: `UserListController.java`
- **Area**: 03 - Excel Export Feature

### Q4: What library is used for excel export?
- [x] **RESOLVED** - Apache POI 5.2.5
- **Dependencies**: poi + poi-ooxml
- **Area**: 03 - Excel Export Feature

### Q5: What is current state of integration-final?
- [x] **RESOLVED** - Current branch is integration-final, ready for feature integration
- **Area**: 01 - Branch Overview

### Q6: Which features from other branches are needed?
- [x] **RESOLVED** - ONLY captcha and excel export from securite-aymen
- **User confirmed**: "just want to add his new features to the integration"
- **Area**: 05 - Integration Strategy

### Q7: Integration approach preference?
- [x] **RESOLVED** - Cherry-pick specific commits (ad2b90a, bb95d02)
- **Area**: 05 - Integration Strategy

### Q8: **CRITICAL** - Captcha and Excel Export Location
- [x] **RESOLVED** - Found in commits ad2b90a and bb95d02 on origin/feature/securite-aymen
- **Area**: 02 & 03 - Feature Research

## Summary
- **Total Questions**: 8
- **Resolved by Research**: 8
- **Pending Human Input**: 0
- **Pending Code Research**: 0
