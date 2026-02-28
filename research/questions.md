# Open Questions

## Answerable by Code (Need More Research - Area 8)

### Q10: Why do agricole users see 0 farms?
- [ ] **IN PROGRESS** — Investigating session and filtering
- **Question**: Database has farms assigned to agricole users, but dashboard shows 0
- **Likely in**: `AgricoleDashboardController.java:84-99`, `SessionManager.java`
- **Assigned to**: Area 08-agricole-integration.md

### Q11: Is SessionManager properly storing current user?
- [ ] **IN PROGRESS** — Need to verify session initialization
- **Question**: Is the user session properly set during login and retrieved in dashboard?
- **Likely in**: `LoginController.java`, `SessionManager.java`
- **Assigned to**: Area 08-agricole-integration.md

### Q12: Is farm filtering logic working?
- [ ] **IN PROGRESS** — Need to test stream filter
- **Question**: Is the stream filter correctly matching farms to current user ID?
- **Likely in**: `AgricoleDashboardController.java:92-95`
- **Assigned to**: Area 08-agricole-integration.md

## Need Human Input

### Q13: Should we add debug logging?
- [ ] **PENDING** — Need user decision
- **Question**: Should we add temporary debug logging to trace the issue?
- **Options**: 
  - Add System.out.println statements
  - Use Java logging framework
  - Skip debug logging and fix directly

### Q1: Which Ferme model version should be used?
- [x] **RESOLVED** — Use Expertise branch version
- **Reason**: Expertise version has proper `idFermier` FK to User, follows Java naming conventions, matches database schema
- **Found in**: `feature/expertise-is-alaeddin:models/Ferme.java`
- **Area**: 03-model-conflicts.md

### Q2: Which LoginController should be used?
- [x] **RESOLVED** — Use Security branch version
- **Reason**: Security version has all Expertise features PLUS face login and OTP verification
- **Found in**: `feature/securite-aymen:controllers/LoginController.java`
- **Area**: 04-controller-conflicts.md

### Q3: Which UserService should be used?
- [x] **RESOLVED** — Use Security branch version
- **Reason**: Security version has UserLogService integration for audit logging
- **Found in**: `feature/securite-aymen:services/UserService.java`
- **Area**: 04-controller-conflicts.md

### Q4: How to handle Animaux/Plantes naming conventions?
- [x] **RESOLVED** — Rename fields to camelCase
- **Reason**: Java naming convention, consistency with other models
- **Changes**: 
  - `id_animal` → `idAnimal`
  - `id_ferme` → `idFerme`
  - `nom_espece` → `nomEspece`
  - `cycle_vie` → `cycleVie`
- **Area**: 03-model-conflicts.md

### Q5: Which ExpertDashboardController to use?
- [x] **RESOLVED** — Use Expertise branch version
- **Reason**: Expertise version has statistics integration (AnalyseService, ConseilService, FermeService)
- **Found in**: `feature/expertise-is-alaeddin:controllers/ExpertDashboardController.java`
- **Area**: 04-controller-conflicts.md

### Q6: How to merge module-info.java?
- [x] **RESOLVED** — Combine all requires statements
- **Details**: Merge all `requires` from all three branches
- **Area**: 02-dependencies.md

## Resolved

- [x] Q1: Ferme model version — Answer: Use Expertise version with idFermier
- [x] Q2: LoginController version — Answer: Use Security version with face login
- [x] Q3: UserService version — Answer: Use Security version with UserLogService
- [x] Q4: Animaux/Plantes naming — Answer: Rename to camelCase
- [x] Q5: ExpertDashboardController — Answer: Use Expertise version with statistics
- [x] Q6: module-info.java merge — Answer: Combine all requires
- [x] Q7: Trefle API key — Answer: Move to Config.java for consistency
- [x] Q8: unique_fermier constraint — Answer: Remove constraint to allow multiple farms per user
- [x] Q9: Email SMTP configuration — Answer: Use environment variables (SMTP_HOST, SMTP_USER, SMTP_PASS)

## Summary
- **Total Questions**: 9
- **Resolved by Research**: 6
- **Resolved by Human Input**: 3
- **All Questions Resolved**: ✅
