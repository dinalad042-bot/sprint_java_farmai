# Open Questions

## Answerable by Code (Need More Research)

### Q1: Where is captcha implemented in securite-aymen?
- [ ] **PENDING** — Need to examine branch diff
- **Question**: In which controller/view is captcha located?
- **Likely locations**: LoginController, SignupController, VerificationController
- **Assigned to**: Area 02 - Captcha Feature

### Q2: What type of captcha is used?
- [ ] **PENDING** — Need to examine implementation
- **Options**: Image captcha, Google reCAPTCHA, math captcha, simple text
- **Assigned to**: Area 02 - Captcha Feature

### Q3: Where is excel export implemented?
- [ ] **PENDING** — Need to examine branch diff
- **Question**: Which controllers have excel export functionality?
- **Assigned to**: Area 03 - Excel Export Feature

### Q4: What library is used for excel export?
- [ ] **PENDING** — Need to check pom.xml
- **Likely**: Apache POI
- **Assigned to**: Area 03 - Excel Export Feature

### Q5: What is current state of integration-final?
- [ ] **PENDING** — Need to examine branch
- **Question**: What's already merged? What's missing?
- **Assigned to**: Area 01 - Branch Overview

## Need Human Input

### Q6: Which features from other branches are needed?
- [ ] **PENDING** — Need user confirmation
- **Question**: Besides captcha and excel export, what else from securite-aymen?
- **Question**: What from expertise-is-alaeddin and ferme-amen?

### Q7: Integration approach preference?
- [ ] **PENDING** — Need user preference
- **Options**:
  - Merge entire securite-aymen branch
  - Cherry-pick only captcha and excel commits
  - Manual copy of specific files
- **Assigned to**: Area 05 - Integration Strategy

### Q8: **CRITICAL** - Captcha and Excel Export Location
- [ ] **PENDING** — Need user clarification
- **Finding**: After thorough search of all branches:
  - `feature/securite-aymen`: Contains Face Recognition, OTP, User Logs, Notifications — **NO captcha or excel**
  - `feature/expertise-is-alaeddin`: Contains Expert analyses, conseils — **NO captcha or excel**
  - `feature/ferme-amen`: Contains Farm management, irrigation — **NO captcha or excel**
  - `integration-final`: Contains merged features — **NO captcha or excel**
  - Current `integration` branch: **NO captcha or excel**
- **Question**: Are captcha and excel export features:
  1. **To be created** (new implementation)?
  2. In a **different branch** not yet pushed to origin?
  3. In **Aymen's local repository** only?
  4. Already **implemented elsewhere** I should look?
- **Assigned to**: Area 02 & 03 - Feature Research

## Resolved
*None yet*

## Summary
- **Total Questions**: 8
- **Resolved by Research**: 0
- **Pending Human Input**: 3
- **Pending Code Research**: 5
