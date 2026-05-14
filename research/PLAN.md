# Implementation Plan

## Overview
Pull Aymen's TODAY'S (March 2, 2026) modifications from `feature/securite-aymen` branch into the current `integration-final` branch. This includes THREE features added today:
1. Captcha (21:21)
2. Excel Export (21:34)
3. User Statistics (21:58)

## Research Summary
Based on 4 research areas investigated:
- [01-branch-overview.md](areas/01-branch-overview.md) - Identified 3 commits from today
- [02-captcha-feature.md](areas/02-captcha-feature.md) - 5 files modified/added
- [03-excel-export-feature.md](areas/03-excel-export-feature.md) - 4 files modified
- [04-statistics-feature.md](areas/04-statistics-feature.md) - 6 files modified/added

## Current State Analysis
**Current Branch**: `integration-final` (HEAD at 9eb8fef)

**Target Commits** (in chronological order):
1. `ad2b90a` - feat: add CAPTCHA to login form (Mon Mar 2 21:21:04 2026)
2. `bb95d02` - feat: export ListView data to Excel (Mon Mar 2 21:34:07 2026)
3. `c137cb5` - feat(statistics): implement user statistics feature (Mon Mar 2 21:58:58 2026)

## Desired End State
- Login screen has captcha verification
- User list screen has Excel export button
- Admin dashboard can open user statistics pie chart
- No other changes from securite-aymen branch

## Out of Scope
- Face recognition features (commit 614aa8d)
- OTP functionality (commit 25fc200)
- Any other commits from securite-aymen before ad2b90a

---

## Implementation Phases

### Phase 1: Cherry-pick Captcha Feature
**Goal**: Add captcha to login form
**Research basis**: [02-captcha-feature.md](areas/02-captcha-feature.md)
**Commit**: `ad2b90a`

**Command**:
```bash
git cherry-pick ad2b90a
```

**What this does**:
| File | Change |
|------|--------|
| `pom.xml` | Adds javafx-swing dependency |
| `module-info.java` | Adds javafx.swing, makes java.desktop transitive |
| `LoginController.java` | Adds captcha validation logic |
| **NEW** `CaptchaUtil.java` | Creates captcha generation utility |
| `login.fxml` | Adds captcha UI (replaces "Remember Me") |

**Success criteria**:
- [ ] Cherry-pick completes without conflicts
- [ ] Build succeeds: `mvn clean compile`

**Depends on**: Nothing

---

### Phase 2: Cherry-pick Excel Export Feature
**Goal**: Add Excel export to user list
**Research basis**: [03-excel-export-feature.md](areas/03-excel-export-feature.md)
**Commit**: `bb95d02`

**Command**:
```bash
git cherry-pick bb95d02
```

**What this does**:
| File | Change |
|------|--------|
| `pom.xml` | Adds Apache POI dependencies (poi + poi-ooxml) |
| `module-info.java` | Adds org.apache.poi.poi and org.apache.poi.ooxml |
| `UserListController.java` | Adds handleExportExcel() method |
| `user-list.fxml` | Adds export button |

**Success criteria**:
- [ ] Cherry-pick completes without conflicts
- [ ] Build succeeds: `mvn clean compile`

**Depends on**: Phase 1

---

### Phase 3: Cherry-pick User Statistics Feature
**Goal**: Add user statistics pie chart
**Research basis**: [04-statistics-feature.md](areas/04-statistics-feature.md)
**Commit**: `c137cb5`

**Command**:
```bash
git cherry-pick c137cb5
```

**What this does**:
| File | Change |
|------|--------|
| `module-info.java` | Opens utils package to javafx.fxml |
| `AdminDashboardController.java` | Adds handleViewStatistics() method |
| **NEW** `UserStatisticsController.java` | Pie chart controller |
| `UserService.java` | Adds getUsersCountByRole() method |
| `admin-dashboard.fxml` | Adds onAction to stats button |
| **NEW** `user-statistics.fxml` | Statistics view with pie chart |

**Success criteria**:
- [ ] Cherry-pick completes without conflicts
- [ ] Build succeeds: `mvn clean compile`

**Depends on**: Phase 2

---

## All Features Summary

### Files Modified (11 total):
1. `pom.xml` - Dependencies (captcha + excel)
2. `module-info.java` - Module requirements (all 3 features)
3. `LoginController.java` - Captcha logic
4. `UserListController.java` - Export logic
5. `AdminDashboardController.java` - Stats window handler
6. `UserService.java` - Statistics query
7. `login.fxml` - Captcha UI
8. `user-list.fxml` - Export button
9. `admin-dashboard.fxml` - Stats button action

### New Files (3 total):
10. `src/main/java/tn/esprit/farmai/utils/CaptchaUtil.java`
11. `src/main/java/tn/esprit/farmai/controllers/UserStatisticsController.java`
12. `src/main/resources/tn/esprit/farmai/views/user-statistics.fxml`

---

## Testing Strategy

### Captcha Testing
1. Navigate to login screen
2. Verify captcha image displays
3. Click refresh button - new captcha should appear
4. Enter wrong captcha - should show error
5. Enter correct captcha - should proceed to login

### Excel Export Testing
1. Navigate to User List (Admin)
2. Click "Exporter Excel" button
3. Save file dialog should open
4. Save as .xlsx file
5. Verify file opens with correct data

### Statistics Testing
1. Navigate to Admin Dashboard
2. Click "Voir Statistiques" button
3. Pie chart window should open
4. Verify chart shows user counts by role
5. Hover over slices to see tooltips

---

## Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Merge conflicts in pom.xml | Medium | High | Resolve manually, keep all new dependencies |
| Merge conflicts in module-info.java | Medium | High | Resolve manually, add all new requires/opens |
| FXML conflicts | Low | Medium | Use current version and add changes manually |

---

## Rollback Plan

If issues occur:
```bash
# Reset to before cherry-picks
git reset --hard 9eb8fef

# Or abort current cherry-pick
git cherry-pick --abort
```

---

## Commands Summary

```bash
# Ensure you're on integration-final
git checkout integration-final

# Pull captcha feature (21:21)
git cherry-pick ad2b90a

# Pull excel feature (21:34)
git cherry-pick bb95d02

# Pull statistics feature (21:58)
git cherry-pick c137cb5

# Build and test
mvn clean compile

# If successful, push
git push origin integration-final
```

---

## Alternative: Manual Application

If automatic cherry-pick fails, apply patches manually:

```bash
# Generate patches
git show ad2b90a > captcha.patch
git show bb95d02 > excel.patch
git show c137cb5 > stats.patch

# Apply patches
git apply captcha.patch
git apply excel.patch
git apply stats.patch
```
