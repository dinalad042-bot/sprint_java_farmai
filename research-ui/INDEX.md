# Research Index - UX/UI Unification

## Goal
Achieve a professional-grade, unified UX/UI for the FarmAI platform by consolidating fragmented styles, standardizing component usage, and implementing live avatar/profile updates across all interfaces.

## Status: 🟢 COMPLETE

## Research Areas

| # | Area | Status | Doc | Key Finding |
|---|------|--------|-----|-------------|
| 1 | CSS Architecture | 🟢 Complete | [01-css-architecture.md](areas/01-css-architecture.md) | Two CSS files (auth.css + dashboard.css) with inconsistent colors and button classes |
| 2 | Utility Classes | 🟢 Complete | [02-utility-classes.md](areas/02-utility-classes.md) | AvatarUtil, AlertUtils, NavigationUtil exist but usage is inconsistent |
| 3 | Session & Profile Management | 🟢 Complete | [03-session-profile.md](areas/03-session-profile.md) | SessionManager has currentUserProperty but not all controllers listen |
| 4 | Dashboard Controllers | 🟢 Complete | [04-dashboard-controllers.md](areas/04-dashboard-controllers.md) | All 4 dashboards have sidebarAvatar but not all bind to property listener |
| 5 | Table & List Views | 🟢 Complete | [05-table-list-views.md](areas/05-table-list-views.md) | GestionAnalyses uses manual ImageView; should use AvatarUtil |
| 6 | Form Validation | 🟢 Complete | [06-form-validation.md](areas/06-form-validation.md) | Validation logic duplicated; no ValidationUtils class exists |
| 7 | Navigation Patterns | 🟢 Complete | [07-navigation-patterns.md](areas/07-navigation-patterns.md) | navigateWithFade() duplicated in multiple controllers |

## Current Position
📍 Research complete - Creating PLAN.md

## Discovery Log (Chronological)
- [2026-03-03 15:55] Started UX/UI unification research
- [2026-03-03 15:56] Found 2 CSS files: auth.css (#2E7D32, #4CAF50) and dashboard.css (#388E3C, #2E7D32) - inconsistent greens
- [2026-03-03 15:57] AvatarUtil is comprehensive but GestionAnalyses uses manual ImageView
- [2026-03-03 15:58] SessionManager.currentUserProperty() exists but not bound in all controllers
- [2026-03-03 15:59] NavigationUtil has navigateWithFade but Expert/Gestion controllers have local copies
- [2026-03-03 16:00] AlertUtils exists with -fx-accent styling but GestionAnalyses uses manual Alert
- [2026-03-03 16:01] SignupController has validation setupValidation() pattern that should be centralized
- [2026-03-03 16:02] All dashboards call ProfileManager.updateProfileUI() but only some add property listeners

## Open Questions Count: 3
See [questions.md](questions.md)

## Blockers
- [ ] None currently

## Summary of Issues Found

### Critical
1. **Generic Avatar Bug**: Controllers load avatar once at init but don't listen for updates
2. **CSS Inconsistency**: Different greens (#2E7D32 vs #388E3C), button classes (.primary-button vs .primary-btn)
3. **Duplicated Navigation**: navigateWithFade() exists in 3+ controllers + NavigationUtil

### High Priority
4. **Manual Alert Creation**: GestionAnalysesController uses `new Alert()` instead of AlertUtils
5. **Table Cell Avatars**: GestionAnalyses uses manual ImageView instead of AvatarUtil.createCircularAvatar()
6. **Validation Duplication**: Email, CIN regex in SignupController and AnalyseDialog - no central ValidationUtils

### Medium Priority
7. **Missing Property Listeners**: FournisseurDashboard doesn't listen to currentUserProperty
8. **Inconsistent Form Padding**: Some use 10px hgap, others 15px

## Next Steps
1. Complete FXML structure research
2. Resolve open questions
3. Create comprehensive PLAN.md
