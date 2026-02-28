# Research Area: Branch Overview

## Status: 🟢 Complete

## What I Need To Learn
- What files does each branch modify/add?
- What is the scope of changes in each branch?
- Which files overlap between branches?

## Files Examined
- [x] `git diff --name-status main..origin/feature/expertise-is-alaeddin` — 69 files changed
- [x] `git diff --name-status main..origin/feature/securite-aymen` — 40 files changed
- [x] `git diff --name-status main..origin/feature/ferme-amen` — 40 files changed

## Findings

### Branch 1: feature/expertise-is-alaeddin (69 files)
**Purpose**: Expert dashboard, Analysis management, Conseil (advice) system

**Key Features**:
- Expert dashboard with statistics
- Analysis (Analyse) CRUD management
- Advice (Conseil) system linked to analyses
- PDF report generation
- Weather integration
- Groq AI API integration for expert chatbot

**Files Added**:
- Models: `Analyse.java`, `Conseil.java`, `Ferme.java`, `Priorite.java`, `UserLog.java`, `UserLogAction.java`
- Controllers: `ExpertDashboardController.java`, `GestionAnalysesController.java`, `GestionConseilsController.java`, `FermierAnalysesController.java`, `AjoutConseilController.java`, `StatisticsController.java`
- Services: `AnalyseService.java`, `ConseilService.java`, `FermeService.java`, `PDFReportService.java`, `UserLogService.java`, `UserService.java`
- Utils: `WeatherUtils.java`, `SpeechUtils.java`, `SimpleHttpClient.java`, `NotificationManager.java`, `SessionManager.java`, `PasswordUtil.java`, `NavigationUtil.java`, `ProfileManager.java`, `Config.java`, `AnalyseDialog.java`, `AlertUtils.java`
- Views: `expert-dashboard.fxml`, `gestion-analyses.fxml`, `gestion-conseils.fxml`, `fermier-analyses.fxml`, `ajout-conseil.fxml`, `statistics.fxml`

### Branch 2: feature/securite-aymen (40 files)
**Purpose**: Security features, Face recognition login, OTP verification, Email integration

**Key Features**:
- Face recognition login using JavaCV/OpenCV
- OTP (One-Time Password) verification
- Email service for password reset
- Face enrollment service
- User logging system

**Files Added**:
- Controllers: `FaceLoginController.java`, `FaceRecognitionController.java`, `VerificationController.java`
- Services: `FaceEnrollmentService.java`, `OTPService.java`, `UserService.java`, `UserLogService.java`
- Utils: `MailingService.java`, `SessionManager.java`, `PasswordUtil.java`, `NavigationUtil.java`, `NotificationManager.java`, `ProfileManager.java`
- Views: `face-login-view.fxml`, `face-recognition-view.fxml`, `verification.fxml`
- Resources: `cascade/haarcascade_frontalface_default.xml`
- SQL: `database/face_data_migration.sql`

### Branch 3: feature/ferme-amen (40 files)
**Purpose**: Farm management, Animals, Plants, Irrigation AI, Weather integration

**Key Features**:
- Farm (Ferme) CRUD management
- Animals (Animaux) management
- Plants (Plantes) management
- Irrigation AI service
- Expert voice service
- Weather service
- Market service
- PDF generation for reports

**Files Added**:
- Models: `Ferme.java`, `Animaux.java`, `Plantes.java`
- Controllers: `FermeController.java`, `AnimauxController.java`, `PlantesController.java`, `AdminController.java`, `AdminMapController.java`, `AgricoIrrigationController.java`
- Services: `ServiceFerme.java`, `ServiceAnimaux.java`, `ServicePlantes.java`, `IrrigationAI.java`, `WeatherService.java`, `MarketService.java`, `PdfGenerator.java`, `ExpertChatbotService.java`, `ExpertVoiceService.java`
- Views: `gestion-fermes.fxml`, `gestion-animaux.fxml`, `gestion-plantes.fxml`, `admin.fxml`, `admin_map.fxml`

## Overlapping Files (CONFLICTS)

### Models
| File | Branches | Conflict Type |
|------|----------|---------------|
| `Ferme.java` | expertise + ferme | **Different structure** - expertise has `idFermier` FK |
| `User.java` | main + ferme | ferme branch DELETES User.java, Role.java |

### Controllers
| File | Branches | Conflict Type |
|------|----------|---------------|
| `LoginController.java` | expertise + security | **Different implementations** - security has face login |
| `AdminDashboardController.java` | expertise + security | Both modify |
| `AgricoleDashboardController.java` | expertise + security | Both modify |
| `ExpertDashboardController.java` | expertise + security | Both modify |
| `FournisseurDashboardController.java` | expertise + security | Both modify |
| `UserListController.java` | expertise + security | Both modify |
| `NotificationsController.java` | expertise + security | Both modify |
| `SignupController.java` | expertise + security | Both modify |

### Services
| File | Branches | Conflict Type |
|------|----------|---------------|
| `UserService.java` | expertise + security | security has UserLogService integration |
| `UserLogService.java` | expertise + security | Both add |

### Utils
| File | Branches | Conflict Type |
|------|----------|---------------|
| `NavigationUtil.java` | expertise + security | Both add |
| `NotificationManager.java` | expertise + security | Both add |
| `PasswordUtil.java` | expertise + security | Same implementation |
| `ProfileManager.java` | expertise + security | Same implementation |
| `SessionManager.java` | expertise + security | Same implementation |

### Views (FXML)
| File | Branches | Conflict Type |
|------|----------|---------------|
| `login.fxml` | expertise + security | security has face login button |
| `admin-dashboard.fxml` | expertise + security | Both modify |
| `agricole-dashboard.fxml` | expertise + security + ferme | All three modify |
| `expert-dashboard.fxml` | expertise + security | Both modify |
| `fournisseur-dashboard.fxml` | expertise + security | Both modify |
| `user-list.fxml` | expertise + security | Both modify |
| `notifications.fxml` | expertise + security | Both modify |
| `signup.fxml` | expertise + security | Both modify |

### Configuration
| File | Branches | Conflict Type |
|------|----------|---------------|
| `pom.xml` | all three | **Different dependencies** |
| `module-info.java` | all three | **Different requires** |

## Code Patterns Observed
- All branches use JavaFX for UI
- All branches use MySQL for database
- All branches follow MVC pattern (models, controllers, services)
- All branches use MyDBConnexion singleton for database connection

## Relevance to Implementation
This overview identifies ALL files that will conflict during merge. The integration strategy must:
1. Merge pom.xml dependencies from all three branches
2. Merge module-info.java requires from all three branches
3. Resolve Ferme.java model conflict (use expertise version with idFermier)
4. Merge LoginController to include both face login AND standard login
5. Merge UserService to include UserLogService integration
6. Combine all unique controllers, services, and views from each branch

## New Questions Generated
- Which Ferme model version should be the base? → sent to questions.md
- How to merge LoginController? → sent to questions.md
- Which UserService version to use? → sent to questions.md

## Status Update
- [x] Initial investigation of all three branches
- [x] Identify file changes in each branch
- [x] Identify overlapping files
- [x] Categorize conflict types
