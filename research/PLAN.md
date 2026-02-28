# Implementation Plan

## Overview
Integrate three feature branches into one unified FarmAI application:
- **feature/expertise-is-alaeddin**: Expert dashboard, Analysis/Conseil management, PDF reports
- **feature/securite-aymen**: Face recognition login, OTP verification, Email service
- **feature/ferme-amen**: Farm management, Animals/Plants CRUD, Irrigation AI

## Research Summary
Based on 7 research areas investigated. Full research at:
- [01-branch-overview.md](areas/01-branch-overview.md) — 69+40+40 files, 15+ conflicts
- [02-dependencies.md](areas/02-dependencies.md) — Merged pom.xml with 20+ dependencies
- [03-model-conflicts.md](areas/03-model-conflicts.md) — Ferme model resolved
- [04-controller-conflicts.md](areas/04-controller-conflicts.md) — LoginController resolved
- [05-database-schema.md](areas/05-database-schema.md) — 8 tables merged
- [06-fxml-conflicts.md](areas/06-fxml-conflicts.md) — Views merged
- [07-utilities.md](areas/07-utilities.md) — Utilities merged

## Current State Analysis

### Branch States
| Branch | Files Changed | Key Features |
|--------|---------------|--------------|
| expertise-is-alaeddin | 69 | Analyse, Conseil, Statistics, PDF, Weather, Groq AI |
| securite-aymen | 40 | Face login, OTP, Email, UserLog |
| ferme-amen | 40 | Ferme, Animaux, Plantes, Irrigation AI, Trefle API |

### Conflicts Identified
1. **Ferme.java** — Different structure (expertise has idFermier FK)
2. **LoginController.java** — Security has face login + OTP
3. **UserService.java** — Security has UserLogService integration
4. **pom.xml** — Different dependencies per branch
5. **module-info.java** — Different requires per branch
6. **login.fxml** — Security has face login button
7. **Dashboard FXMLs** — Multiple branches modify

## Desired End State
- Single `integration` branch with all features working together
- Unified database schema with 8 tables
- Combined pom.xml with all dependencies
- All controllers, services, models, and views merged
- Face login + Standard login working
- Expert dashboard with statistics
- Farm management with Animals/Plants

**Verification Method**: Application compiles, runs, and all features accessible

## Out of Scope
- `integration/all-features-comprehensive` branch (explicitly ignored)
- Bug fixes not related to integration
- New feature development
- Performance optimization

---

## Implementation Phases

### Phase 1: Setup & Branch Creation
**Goal**: Create integration branch and prepare for merge

**Research basis**: 01-branch-overview.md

**Changes**:
| File | Change | Why |
|------|--------|-----|
| New branch `integration` | Create from main | Clean starting point |
| `.gitignore` | Update | Ignore build artifacts |

**Commands**:
```bash
cd C:\Users\sliti\Documents\pro\integration\sprint_java_farmai
git checkout main
git pull origin main
git checkout -b integration
```

**Success criteria**:
Automated:
- [ ] `git branch` shows `* integration`
- [ ] `git log -1` shows main as base

**Depends on**: Nothing

---

### Phase 2: Merge pom.xml Dependencies
**Goal**: Combine all dependencies from three branches

**Research basis**: 02-dependencies.md

**Changes**:
| File | Change | Why |
|------|--------|-----|
| `pom.xml` | Merge dependencies | All features need their libraries |

**Detailed changes**:
Add to pom.xml:
```xml
<!-- Face Recognition (Security) -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.10</version>
</dependency>

<!-- Email (Security) -->
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>jakarta.mail</artifactId>
    <version>2.0.1</version>
</dependency>
<dependency>
    <groupId>com.sun.activation</groupId>
    <artifactId>jakarta.activation</artifactId>
    <version>2.0.1</version>
</dependency>

<!-- HTTP Client (Expertise) -->
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.14</version>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20231013</version>
</dependency>

<!-- Charts (Expertise) -->
<dependency>
    <groupId>org.jfree</groupId>
    <artifactId>jfreechart</artifactId>
    <version>1.5.3</version>
</dependency>

<!-- PDF Generation -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.29</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>kernel</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>io</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>layout</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>font-asian</artifactId>
    <version>7.2.5</version>
</dependency>

<!-- Voice/Speech (Ferme) -->
<dependency>
    <groupId>net.sf.freetts</groupId>
    <artifactId>freetts</artifactId>
    <version>1.2.2</version>
</dependency>
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-speech</artifactId>
    <version>2.26.0</version>
</dependency>

<!-- Testing (Security) -->
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-launcher</artifactId>
    <version>1.9.2</version>
</dependency>
```

**Success criteria**:
Automated:
- [ ] `mvn dependency:resolve` succeeds
- [ ] `mvn compile` succeeds

**Depends on**: Phase 1

---

### Phase 3: Merge module-info.java
**Goal**: Combine all module requires

**Research basis**: 02-dependencies.md

**Changes**:
| File | Change | Why |
|------|--------|-----|
| `src/main/java/module-info.java` | Merge requires | All modules need to be accessible |

**Detailed changes**:
```java
module tn.esprit.farmai {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.media;
    
    // UI Components
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    
    // Database
    requires java.sql;
    requires mysql.connector.j;
    
    // Security (Face Recognition)
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    requires java.desktop;
    
    // Email
    requires jakarta.mail;
    requires jakarta.activation;
    
    // PDF
    requires org.apache.pdfbox;
    
    // iText PDF
    requires kernel;
    requires layout;
    requires io;
    
    // JSON
    requires org.json;
    
    // JavaScript (for WebView)
    requires jdk.jsobject;
    
    // HTTP
    requires java.net.http;
    
    // Testing
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.platform.commons;
    requires org.junit.platform.engine;
    requires org.junit.platform.launcher;
    
    // Main package
    opens tn.esprit.farmai to javafx.fxml;
    exports tn.esprit.farmai;
    
    // Controllers package
    opens tn.esprit.farmai.controllers to javafx.fxml;
    exports tn.esprit.farmai.controllers;
    
    // Models package
    opens tn.esprit.farmai.models to javafx.fxml, javafx.base;
    exports tn.esprit.farmai.models;
    
    // Services package
    exports tn.esprit.farmai.services;
    
    // Utils package
    exports tn.esprit.farmai.utils;
    
    // Interfaces package
    exports tn.esprit.farmai.interfaces;
    
    // Test package
    opens tn.esprit.farmai.test to javafx.fxml, javafx.graphics, 
        org.junit.platform.commons, org.junit.platform.engine;
    exports tn.esprit.farmai.test;
}
```

**Success criteria**:
Automated:
- [ ] `mvn compile` succeeds

**Depends on**: Phase 2

---

### Phase 4: Merge Models
**Goal**: Integrate all model classes with conflict resolution

**Research basis**: 03-model-conflicts.md

**Changes**:
| File | Change | Why |
|------|--------|-----|
| `models/Ferme.java` | Use Expertise version | Has idFermier FK |
| `models/Animaux.java` | Add from Ferme + fix naming | New feature, fix snake_case |
| `models/Plantes.java` | Add from Ferme + fix naming | New feature, fix snake_case |
| `models/Analyse.java` | Add from Expertise | New feature |
| `models/Conseil.java` | Add from Expertise | New feature |
| `models/Priorite.java` | Add from Expertise | Enum for Conseil |
| `models/UserLog.java` | Add from Security | Audit logging |
| `models/UserLogAction.java` | Add from Security | Enum for UserLog |
| `models/User.java` | Keep from main | DO NOT DELETE |
| `models/Role.java` | Keep from main | DO NOT DELETE |

**Animaux.java Fixed Version**:
```java
package tn.esprit.farmai.models;

import java.sql.Date;
import java.util.Objects;

public class Animaux {
    private int idAnimal;        // Fixed: was id_animal
    private String espece;
    private String etatSante;    // Fixed: was etat_sante
    private Date dateNaissance;  // Fixed: was date_naissance
    private int idFerme;         // Fixed: was id_ferme
    
    public Animaux() {}
    
    public Animaux(int idAnimal, String espece, String etatSante, 
                   Date dateNaissance, int idFerme) {
        this.idAnimal = idAnimal;
        this.espece = espece;
        this.etatSante = etatSante;
        this.dateNaissance = dateNaissance;
        this.idFerme = idFerme;
    }
    
    // Getters and setters with camelCase names
    // ... (standard getters/setters)
}
```

**Plantes.java Fixed Version**:
```java
package tn.esprit.farmai.models;

import java.util.Objects;

public class Plantes {
    private int idPlante;        // Fixed: was id_plante
    private String nomEspece;    // Fixed: was nom_espece
    private String cycleVie;     // Fixed: was cycle_vie
    private int idFerme;         // Fixed: was id_ferme
    private double quantite;
    
    public Plantes() {}
    
    public Plantes(int idPlante, String nomEspece, String cycleVie, 
                   int idFerme, double quantite) {
        this.idPlante = idPlante;
        this.nomEspece = nomEspece;
        this.cycleVie = cycleVie;
        this.idFerme = idFerme;
        this.quantite = quantite;
    }
    
    // Getters and setters with camelCase names
    // ... (standard getters/setters)
}
```

**Success criteria**:
Automated:
- [ ] `mvn compile` succeeds
Manual:
- [ ] All model classes present
- [ ] No snake_case field names

**Depends on**: Phase 3

---

### Phase 5: Merge Services
**Goal**: Integrate all service classes

**Research basis**: 03-model-conflicts.md, 04-controller-conflicts.md

**Changes**:
| File | Change | Why |
|------|--------|-----|
| `services/UserService.java` | Use Security version | Has UserLogService integration |
| `services/UserLogService.java` | Add | Audit logging |
| `services/AnalyseService.java` | Add from Expertise | Analysis CRUD |
| `services/ConseilService.java` | Add from Expertise | Advice CRUD |
| `services/FermeService.java` | Add from Expertise | Farm CRUD |
| `services/FaceEnrollmentService.java` | Add from Security | Face recognition |
| `services/OTPService.java` | Add from Security | OTP generation |
| `services/PDFReportService.java` | Add from Expertise | PDF reports |
| `services/ServiceAnimaux.java` | Add from Ferme + fix naming | Animals CRUD |
| `services/ServicePlantes.java` | Add from Ferme + fix naming | Plants CRUD |
| `services/IrrigationAI.java` | Add from Ferme | Irrigation AI |
| `services/WeatherService.java` | Add from Ferme | Weather API |
| `services/MarketService.java` | Add from Ferme | Market data |
| `services/PdfGenerator.java` | Add from Ferme | PDF generation |
| `services/ExpertChatbotService.java` | Add from Ferme | Chatbot |
| `services/ExpertVoiceService.java` | Add from Ferme | Voice service |

**ServiceAnimaux.java Fixed Version**:
Update field references from snake_case to camelCase:
- `id_animal` → `idAnimal`
- `id_ferme` → `idFerme`

**ServicePlantes.java Fixed Version**:
Update field references from snake_case to camelCase:
- `id_plante` → `idPlante`
- `nom_espece` → `nomEspece`
- `cycle_vie` → `cycleVie`
- `id_ferme` → `idFerme`

**Success criteria**:
Automated:
- [ ] `mvn compile` succeeds

**Depends on**: Phase 4

---

### Phase 6: Merge Controllers
**Goal**: Integrate all controllers with conflict resolution

**Research basis**: 04-controller-conflicts.md

**Changes**:
| File | Change | Why |
|------|--------|-----|
| `controllers/LoginController.java` | Use Security version | Has face login + OTP |
| `controllers/ExpertDashboardController.java` | Use Expertise version | Has statistics |
| `controllers/AdminDashboardController.java` | Merge | Both modify |
| `controllers/AgricoleDashboardController.java` | Merge | All three modify |
| `controllers/FournisseurDashboardController.java` | Merge | Both modify |
| `controllers/UserListController.java` | Merge | Both modify |
| `controllers/NotificationsController.java` | Merge | Both modify |
| `controllers/SignupController.java` | Merge | Both modify |
| `controllers/UserLogController.java` | Add | Audit log view |
| `controllers/FaceLoginController.java` | Add from Security | Face login popup |
| `controllers/FaceRecognitionController.java` | Add from Security | Camera capture |
| `controllers/VerificationController.java` | Add from Security | OTP verification |
| `controllers/GestionAnalysesController.java` | Add from Expertise | Analysis management |
| `controllers/GestionConseilsController.java` | Add from Expertise | Advice management |
| `controllers/FermierAnalysesController.java` | Add from Expertise | Farmer analysis view |
| `controllers/AjoutConseilController.java` | Add from Expertise | Add advice dialog |
| `controllers/StatisticsController.java` | Add from Expertise | Statistics dashboard |
| `controllers/FermeController.java` | Add from Ferme | Farm management |
| `controllers/AnimauxController.java` | Add from Ferme | Animals management |
| `controllers/PlantesController.java` | Add from Ferme | Plants management |
| `controllers/AdminController.java` | Add from Ferme | Admin panel |
| `controllers/AdminMapController.java` | Add from Ferme | Map view |
| `controllers/AgricoIrrigationController.java` | Add from Ferme | Irrigation AI |

**Success criteria**:
Automated:
- [ ] `mvn compile` succeeds

**Depends on**: Phase 5

---

### Phase 7: Merge Utility Classes
**Goal**: Integrate all utility classes

**Research basis**: 07-utilities.md

**Changes**:
| File | Change | Why |
|------|--------|-----|
| `utils/SessionManager.java` | Use either (identical) | Session management |
| `utils/PasswordUtil.java` | Use either (identical) | Password hashing |
| `utils/ProfileManager.java` | Use either (identical) | Profile UI |
| `utils/NavigationUtil.java` | Use Security version | More complete |
| `utils/NotificationManager.java` | Use either (identical) | Notifications |
| `utils/MailingService.java` | Add from Security | Email service |
| `utils/WeatherUtils.java` | Add from Expertise | Weather API |
| `utils/SpeechUtils.java` | Add from Expertise | Text-to-speech |
| `utils/SimpleHttpClient.java` | Add from Expertise | HTTP client |
| `utils/Config.java` | Add from Expertise | Configuration |
| `utils/AnalyseDialog.java` | Add from Expertise | Analysis dialog |
| `utils/AlertUtils.java` | Add from Expertise | Alert dialogs |

**Success criteria**:
Automated:
- [ ] `mvn compile` succeeds

**Depends on**: Phase 6

---

### Phase 8: Merge FXML Views
**Goal**: Integrate all FXML files

**Research basis**: 06-fxml-conflicts.md

**Changes**:
| File | Change | Why |
|------|--------|-----|
| `views/login.fxml` | Use Security version | Has face login button |
| `views/expert-dashboard.fxml` | Use Expertise version | Has statistics |
| `views/admin-dashboard.fxml` | Merge | Both modify |
| `views/agricole-dashboard.fxml` | Merge | All three modify |
| `views/fournisseur-dashboard.fxml` | Merge | Both modify |
| `views/user-list.fxml` | Merge | Both modify |
| `views/notifications.fxml` | Merge | Both modify |
| `views/signup.fxml` | Merge | Both modify |
| `views/UserLogView.fxml` | Add | Audit log view |
| `views/face-login-view.fxml` | Add from Security | Face login popup |
| `views/face-recognition-view.fxml` | Add from Security | Camera capture |
| `views/verification.fxml` | Add from Security | OTP verification |
| `views/gestion-analyses.fxml` | Add from Expertise | Analysis management |
| `views/gestion-conseils.fxml` | Add from Expertise | Advice management |
| `views/fermier-analyses.fxml` | Add from Expertise | Farmer analysis view |
| `views/ajout-conseil.fxml` | Add from Expertise | Add advice dialog |
| `views/statistics.fxml` | Add from Expertise | Statistics dashboard |
| `views/gestion-fermes.fxml` | Add from Ferme | Farm management |
| `views/gestion-animaux.fxml` | Add from Ferme | Animals management |
| `views/gestion-plantes.fxml` | Add from Ferme | Plants management |
| `views/admin.fxml` | Add from Ferme | Admin panel |
| `views/admin_map.fxml` | Add from Ferme | Map view |

**CSS Files**:
- `styles/auth.css` — Use either (identical)
- `styles/dashboard.css` — Use either (identical)

**Resources**:
- `cascade/haarcascade_frontalface_default.xml` — Add from Security (OpenCV face detection)

**Success criteria**:
Automated:
- [ ] `mvn compile` succeeds

**Depends on**: Phase 7

---

### Phase 9: Create Database Schema
**Goal**: Create merged database schema

**Research basis**: 05-database-schema.md

**Changes**:
| File | Change | Why |
|------|--------|-----|
| `database/farmai_complete.sql` | Create | Merged schema for all features |

**SQL Script**:
```sql
-- See 05-database-schema.md for complete SQL
-- Tables: user, ferme, analyse, conseil, animaux, plantes, face_data, user_log
```

**Success criteria**:
Manual:
- [ ] SQL script executes without errors
- [ ] All 8 tables created
- [ ] Foreign keys established

**Depends on**: Phase 8

---

### Phase 10: Final Integration & Testing
**Goal**: Verify all features work together

**Research basis**: All areas

**Changes**:
| File | Change | Why |
|------|--------|-----|
| `HelloApplication.java` | Update entry point | Main application class |

**Success criteria**:
Automated:
- [ ] `mvn clean compile` succeeds
- [ ] `mvn test` passes
Manual:
- [ ] Application starts
- [ ] Login page shows face login button
- [ ] Standard login works
- [ ] Face login works (if camera available)
- [ ] Expert dashboard shows statistics
- [ ] Farm management accessible
- [ ] Animals/Plants CRUD works
- [ ] Analysis/Conseil CRUD works

**Depends on**: Phase 9

---

## Testing Strategy

### Unit Tests
- Run existing tests from all branches
- `mvn test` should pass

### Integration Tests
1. Login flow (standard + face)
2. User registration
3. Farm CRUD
4. Analysis CRUD
5. Conseil CRUD
6. Animals CRUD
7. Plants CRUD
8. PDF generation
9. Statistics display

### Manual Tests
1. Start application: `mvn javafx:run`
2. Test login with: `admin@farmai.tn` / password
3. Navigate all dashboards
4. Test CRUD operations
5. Test face login (if camera available)

---

## Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Dependency version conflict | Low | High | All versions compatible |
| Module access issues | Medium | Medium | Combined module-info |
| Database migration issues | Low | High | Complete SQL script |
| Face recognition native libs | Medium | Medium | javacv-platform bundles natives |
| Google Cloud Speech size | Medium | Low | Optional dependency |

---

## All Files Referenced

### Models (12 files)
- `User.java`, `Role.java` (main)
- `Ferme.java`, `Analyse.java`, `Conseil.java`, `Priorite.java` (expertise)
- `UserLog.java`, `UserLogAction.java` (security/expertise)
- `Animaux.java`, `Plantes.java` (ferme)

### Controllers (23 files)
- `LoginController.java`, `SignupController.java`, `AdminDashboardController.java`, `AgricoleDashboardController.java`, `ExpertDashboardController.java`, `FournisseurDashboardController.java`, `UserListController.java`, `NotificationsController.java`, `UserLogController.java` (shared)
- `FaceLoginController.java`, `FaceRecognitionController.java`, `VerificationController.java` (security)
- `GestionAnalysesController.java`, `GestionConseilsController.java`, `FermierAnalysesController.java`, `AjoutConseilController.java`, `StatisticsController.java` (expertise)
- `FermeController.java`, `AnimauxController.java`, `PlantesController.java`, `AdminController.java`, `AdminMapController.java`, `AgricoIrrigationController.java` (ferme)

### Services (16 files)
- `UserService.java`, `UserLogService.java` (security/expertise)
- `FaceEnrollmentService.java`, `OTPService.java` (security)
- `AnalyseService.java`, `ConseilService.java`, `FermeService.java`, `PDFReportService.java` (expertise)
- `ServiceAnimaux.java`, `ServicePlantes.java`, `ServiceFerme.java`, `IrrigationAI.java`, `WeatherService.java`, `MarketService.java`, `PdfGenerator.java`, `ExpertChatbotService.java`, `ExpertVoiceService.java` (ferme)

### Utils (12 files)
- `MyDBConnexion.java` (main)
- `SessionManager.java`, `PasswordUtil.java`, `ProfileManager.java`, `NavigationUtil.java`, `NotificationManager.java` (shared)
- `MailingService.java` (security)
- `WeatherUtils.java`, `SpeechUtils.java`, `SimpleHttpClient.java`, `Config.java`, `AnalyseDialog.java`, `AlertUtils.java` (expertise)

### Views (22 FXML files)
- `login.fxml`, `signup.fxml`, `admin-dashboard.fxml`, `agricole-dashboard.fxml`, `expert-dashboard.fxml`, `fournisseur-dashboard.fxml`, `user-list.fxml`, `notifications.fxml`, `UserLogView.fxml` (shared)
- `face-login-view.fxml`, `face-recognition-view.fxml`, `verification.fxml` (security)
- `gestion-analyses.fxml`, `gestion-conseils.fxml`, `fermier-analyses.fxml`, `ajout-conseil.fxml`, `statistics.fxml` (expertise)
- `gestion-fermes.fxml`, `gestion-animaux.fxml`, `gestion-plantes.fxml`, `admin.fxml`, `admin_map.fxml` (ferme)

### Configuration (3 files)
- `pom.xml`
- `module-info.java`
- `database/farmai_complete.sql`

---

## Execution Order
1. Phase 1: Setup & Branch Creation
2. Phase 2: Merge pom.xml Dependencies
3. Phase 3: Merge module-info.java
4. Phase 4: Merge Models
5. Phase 5: Merge Services
6. Phase 6: Merge Controllers
7. Phase 7: Merge Utility Classes
8. Phase 8: Merge FXML Views
9. Phase 9: Create Database Schema
10. Phase 10: Final Integration & Testing
