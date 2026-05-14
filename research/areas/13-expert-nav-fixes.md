# Research Area: Expert Interface Navigation Fixes

## Status: 🟢 Complete

## What I Need To Learn
- Which sidebar buttons lack onAction handlers
- What navigation methods exist in GestionAnalysesController
- What navigation methods exist in GestionConseilsController

## Files Examined
- [x] `src/main/resources/tn/esprit/farmai/views/gestion-analyses.fxml:45-52` — Sidebar buttons lack onAction
- [x] `src/main/resources/tn/esprit/farmai/views/gestion-conseils.fxml:45-52` — Sidebar buttons lack onAction
- [x] `src/main/java/tn/esprit/farmai/controllers/GestionAnalysesController.java` — Has handleBack() method
- [x] `src/main/java/tn/esprit/farmai/controllers/GestionConseilsController.java` — Has handleBack() method

## Findings

### Gestion Analyses View (gestion-analyses.fxml)
**Location**: Lines 45-52
**Missing onAction handlers**:
```xml
<Button text="Tableau de bord" styleClass="nav-button" maxWidth="Infinity"/>  <!-- NO onAction -->
<Button text="Analyses" styleClass="nav-button active" maxWidth="Infinity"/>  <!-- NO onAction -->
<Button text="Conseils" styleClass="nav-button" maxWidth="Infinity"/>  <!-- NO onAction -->
```

### Gestion Conseils View (gestion-conseils.fxml)
**Location**: Lines 45-52
**Missing onAction handlers**:
```xml
<Button text="🏠  Tableau de bord" styleClass="nav-button" maxWidth="Infinity"/>  <!-- NO onAction -->
<Button text="🔬  Analyses" styleClass="nav-button" maxWidth="Infinity"/>  <!-- NO onAction -->
<Button text="💡  Conseils" styleClass="nav-button active" maxWidth="Infinity"/>  <!-- NO onAction -->
```

### Required Navigation Targets
| Button | Target View | Controller Method |
|--------|-------------|-------------------|
| Tableau de bord | expert-dashboard.fxml | navigateToDashboard() |
| Analyses | gestion-analyses.fxml | handleConsultations() |
| Conseils | gestion-conseils.fxml | handleRecommendations() |

## Code Patterns Observed
- GestionConseilsController has `handleBack()` that navigates to expert-dashboard.fxml
- GestionAnalysesController has `handleBack()` that navigates to dashboard
- Navigation pattern: `navigateWithFade(fxmlPath, title)`

## Relevance to Implementation
Expert users need to navigate between dashboard, analyses, and conseils views using the sidebar. Currently these buttons are non-functional.

## Implementation Required

### For GestionAnalysesController:
1. Add `handleDashboard()` method - navigate to expert-dashboard.fxml
2. Add `handleConseils()` method - navigate to gestion-conseils.fxml
3. Update gestion-analyses.fxml to add onAction handlers

### For GestionConseilsController:
1. Add `handleDashboard()` method - navigate to expert-dashboard.fxml
2. Add `handleAnalyses()` method - navigate to gestion-analyses.fxml
3. Update gestion-conseils.fxml to add onAction handlers

## Status Update
- [x] Identified missing onAction handlers in both FXML files
- [x] Analyzed existing navigation patterns
- [ ] Add navigation methods to controllers
- [ ] Update FXML files with onAction handlers
