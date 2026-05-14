# Research Area: Model Conflicts

## Status: 🟢 Complete

## What I Need To Learn
- Which models conflict between branches?
- What are the structural differences?
- How to resolve conflicts?

## Files Examined
- [x] `feature/expertise-is-alaeddin:src/main/java/tn/esprit/farmai/models/Ferme.java`
- [x] `feature/ferme-amen:src/main/java/tn/esprit/farmai/models/Ferme.java`
- [x] `feature/expertise-is-alaeddin:src/main/java/tn/esprit/farmai/models/Analyse.java`
- [x] `feature/expertise-is-alaeddin:src/main/java/tn/esprit/farmai/models/Conseil.java`
- [x] `feature/expertise-is-alaeddin:src/main/java/tn/esprit/farmai/models/Priorite.java`
- [x] `feature/ferme-amen:src/main/java/tn/esprit/farmai/models/Animaux.java`
- [x] `feature/ferme-amen:src/main/java/tn/esprit/farmai/models/Plantes.java`
- [x] `feature/expertise-is-alaeddin:src/main/java/tn/esprit/farmai/models/UserLog.java`
- [x] `feature/expertise-is-alaeddin:src/main/java/tn/esprit/farmai/models/UserLogAction.java`

## Findings

### CRITICAL CONFLICT: Ferme.java

#### Expertise Branch Version (RECOMMENDED)
**Location**: `feature/expertise-is-alaeddin:src/main/java/tn/esprit/farmai/models/Ferme.java`

```java
public class Ferme {
    private int idFerme;           // Standard naming
    private String nomFerme;       // Standard naming
    private String lieu;
    private double surface;        // en hectares
    private int idFermier;         // FK to User - IMPORTANT!
    
    // toString() returns: "idFerme - nomFerme (lieu)" for ComboBox display
}
```

**Database Schema**:
```sql
CREATE TABLE `ferme` (
    `id_ferme` int(11) NOT NULL,
    `nom_ferme` varchar(100) NOT NULL,
    `lieu` varchar(255) NOT NULL,
    `surface` double DEFAULT 0,
    `id_fermier` int(11) NOT NULL,  -- FK to user table
    `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
    `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
);
```

#### Ferme Branch Version (INCOMPLETE)
**Location**: `feature/ferme-amen:src/main/java/tn/esprit/farmai/models/Ferme.java`

```java
public class Ferme {
    private int id_ferme;          // Snake_case naming (BAD)
    private String nom_ferme;      // Snake_case naming (BAD)
    private String lieu;
    private float surface;         // float instead of double
    
    // NO idFermier FK - Missing relationship to User!
}
```

**Issues with Ferme Branch Version**:
1. Uses snake_case naming convention (not Java standard)
2. Missing `idFermier` foreign key to User
3. Uses `float` instead of `double` for surface
4. No `toString()` for ComboBox display

### RESOLUTION: Use Expertise Branch Version
**Reason**: 
- Expertise version follows Java naming conventions
- Has proper FK relationship to User (idFermier)
- Matches database schema from `farmai (semifinal).sql`
- Has useful `toString()` for UI display

### Models Unique to Expertise Branch

#### Analyse.java
```java
public class Analyse {
    private int idAnalyse;
    private LocalDateTime dateAnalyse;
    private String resultatTechnique;
    private int idTechnicien;      // FK to User
    private int idFerme;           // FK to Ferme
    private String imageUrl;       // URL for visual documentation
}
```

#### Conseil.java
```java
public class Conseil {
    private int idConseil;
    private String descriptionConseil;
    private Priorite priorite;     // Enum: HAUTE, MOYENNE, BASSE
    private int idAnalyse;         // FK to Analyse
}
```

#### Priorite.java (Enum)
```java
public enum Priorite {
    HAUTE, MOYENNE, BASSE
}
```

### Models Unique to Ferme Branch

#### Animaux.java
```java
public class Animaux {
    private int id_animal;         // Snake_case (needs fixing)
    private String espece;
    private String etat_sante;
    private Date date_naissance;
    private int id_ferme;          // FK to Ferme
}
```

**Note**: Uses snake_case naming - should be renamed to:
- `idAnimal` (was `id_animal`)
- `idFerme` (was `id_ferme`)

#### Plantes.java
```java
public class Plantes {
    private int id_plante;         // Snake_case (needs fixing)
    private String nom_espece;
    private String cycle_vie;
    private int id_ferme;          // FK to Ferme
    private double quantite;
}
```

**Note**: Uses snake_case naming - should be renamed to:
- `idPlante` (was `id_plante`)
- `nomEspece` (was `nom_espece`)
- `cycleVie` (was `cycle_vie`)
- `idFerme` (was `id_ferme`)

### Models Shared by Expertise + Security

#### UserLog.java
```java
public class UserLog {
    private int idLog;
    private int userId;
    private UserLogAction action;  // CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    private String performedBy;
    private String description;
    private LocalDateTime timestamp;
}
```

#### UserLogAction.java (Enum)
```java
public enum UserLogAction {
    CREATE, UPDATE, DELETE, LOGIN, LOGOUT
}
```

### Models Deleted by Ferme Branch
- `User.java` - DELETED (but needed!)
- `Role.java` - DELETED (but needed!)

**CRITICAL**: Ferme branch deletes User.java and Role.java, but these are essential for the application. Must NOT include this deletion.

## Model Integration Strategy

### 1. Keep from Expertise Branch (Priority)
- `Ferme.java` (with idFermier)
- `Analyse.java`
- `Conseil.java`
- `Priorite.java`
- `UserLog.java`
- `UserLogAction.java`

### 2. Add from Ferme Branch (with fixes)
- `Animaux.java` - Rename fields to camelCase
- `Plantes.java` - Rename fields to camelCase

### 3. Keep from Main/Security Branch
- `User.java` (DO NOT DELETE)
- `Role.java` (DO NOT DELETE)

### 4. Add from Security Branch
- Face recognition uses existing User model

## Database Schema Implications

### Required Tables (Merged)
```sql
-- Core tables (from expertise)
user (id_user, nom, prenom, email, password, cin, adresse, telephone, image_url, role)
ferme (id_ferme, nom_ferme, lieu, surface, id_fermier)
analyse (id_analyse, date_analyse, resultat_technique, id_technicien, id_ferme, image_url)
conseil (id_conseil, description_conseil, priorite, id_analyse)

-- Security tables (from security)
face_data (id, user_id, face_model, created_at, updated_at)
user_log (id_log, user_id, action, performed_by, description, timestamp)

-- Ferme tables (from ferme - need schema)
animaux (id_animal, espece, etat_sante, date_naissance, id_ferme)
plantes (id_plante, nom_espece, cycle_vie, id_ferme, quantite)
```

## Relevance to Implementation
The model conflicts are critical. Using the wrong Ferme.java will break the relationship between farms and users. The naming convention inconsistencies in Animaux.java and Plantes.java need to be fixed for Java standards.

## Status Update
- [x] Identified Ferme.java conflict
- [x] Analyzed structural differences
- [x] Determined resolution strategy
- [x] Identified naming convention issues in Animaux/Plantes
- [x] Noted User.java deletion issue in ferme branch
