# FarmIA - Documentation Technique Module CRUD
## Validation Session - Module Analyse & Coneil

---

## 1. Architecture & Emplacement du Codeg

### StruPackages

| Package | Fichier | Role | Ligne de Reference |
|---------|---------|------|-------------------|
| `tn.esprit.farmai.utils` | `MyDBConnexion.java` | **Pattern Singleton** | Lignes 14-34: `getInstance()` |
| `tn.esprit.farmai.models` | `Analyse.java` | Entite (cote 1 de la relation 1:N) | Lignes 9-115 |
| `tn.esprit.farmai.models` | `Conseil.java` | Entite (cote N de la relation 1:N) | Lignes 8-88, FK ligne 13 |
| `tn.esprit.farmai.models` | `Priorite.java` | Enum pour priorite | Lignes 7-26 |
| `tn.esprit.farmai.interfaces` | `CRUD.java` | Interface generique CRUD | Lignes 10-39 |
| `tn.esprit.farmai.services` | `AnalyseService.java` | Implementation Service | Lignes 30-382 |
| `tn.esprit.farmai.services` | `ConseilService.java` | Implementation Service | Lignes 18-161 |
| `tn.esprit.farmai.controllers` | `GestionAnalysesController.java` | TableView JavaFX | Lignes 314-523 |
| `tn.esprit.farmai.controllers` | `AjoutConseilController.java` | ComboBox 1:N | Lignes 33, 83-132 |
| `resources/.../views/` | `*.fxml` | Vues Scene Builder | 13 fichiers FXML |

### Arborescence du Projet

```
tn.esprit.farmai/
|
+-- utils/
|   +-- MyDBConnexion.java      <-- Pattern Singleton
|   +-- Config.java              <-- Configuration API
|   +-- NavigationUtil.java      <-- Navigation entre scenes
|   +-- AnalyseDialog.java       <-- Validation formulaire
|   +-- SimpleHttpClient.java    <-- Client HTTP pour API
|
+-- models/
|   +-- Analyse.java             <-- Entite (parent 1:N)
|   +-- Conseil.java             <-- Entite (enfant 1:N)
|   +-- Priorite.java            <-- Enum (BASSE, MOYENNE, HAUTE)
|   +-- User.java
|   +-- Role.java
|
+-- interfaces/
|   +-- CRUD.java                <-- Interface generique
|
+-- services/
|   +-- AnalyseService.java      <-- CRUD + IA + PDF
|   +-- ConseilService.java      <-- CRUD
|   +-- PDFReportService.java    <-- Rapports US9
|   +-- UserService.java
|
+-- controllers/
|   +-- GestionAnalysesController.java
|   +-- GestionConseilsController.java
|   +-- AjoutConseilController.java
|   +-- StatisticsController.java
|   +-- LoginController.java
|
+-- resources/tn/esprit/farmai/
    +-- views/                   <-- Fichiers FXML (Scene Builder)
    +-- styles/
```

---

## 2. Procedure de Lancement

### Prerequis
- **IDE**: IntelliJ IDEA (derniere version)
- **JDK**: Java 17
- **Base de donnees**: MySQL Server

### Etapes de Lancement

```
1. Ouvrir IntelliJ IDEA
2. File -> Open -> Selectionner le dossier projet
3. Attendre la resolution des dependances Maven
4. Verifier pom.xml contient:
   - mysql-connector-j (8.2.0)
   - javafx-controls (17.0.6)
   - javafx-fxml (17.0.6)
5. Base de donnees:
   - Creer la base 'farmai' via database/farmai.sql
   - Verifier connexion: localhost:3306/farmai (root / mot de passe vide)
6. Lancer l'application:
   - Maven -> Plugins -> javafx -> javafx:run
   - OU terminal: mvn clean javafx:run
7. Connexion:
   - Email: expert@farmai.tn
   - Mot de passe: admin123
```

---

## 3. Points de Demonstration (Demo Checkpoints)

### 3.1 Persistence Securisee (PreparedStatement)

**Fichier**: `AnalyseService.java`

```java
// Lignes 43-56: Methode insertOne avec PreparedStatement
@Override
public void insertOne(Analyse analyse) throws SQLException {
    String query = "INSERT INTO analyse (date_analyse, resultat_technique, id_technicien, id_ferme, image_url) " +
                  "VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
        ps.setTimestamp(1, Timestamp.valueOf(analyse.getDateAnalyse()));
        ps.setString(2, analyse.getResultatTechnique());
        ps.setInt(3, analyse.getIdTechnicien());
        ps.setInt(4, analyse.getIdFerme());
        ps.setString(5, analyse.getImageUrl());
        ps.executeUpdate();
        // ...
    }
}
```

**Fichier**: `ConseilService.java`
```java
// Lignes 31-42: PreparedStatement pour Conseil
@Override
public void insertOne(Conseil conseil) throws SQLException {
    String query = "INSERT INTO conseil (description_conseil, priorite, id_analyse) " +
                  "VALUES (?, ?, ?)";
    try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, conseil.getDescriptionConseil());
        ps.setString(2, conseil.getPriorite().name());
        ps.setInt(3, conseil.getIdAnalyse());  // FK binding
        ps.executeUpdate();
    }
}
```

### 3.2 Gestion de la Relation 1:N

**Controller - ComboBox pour liaison**

**Fichier**: `AjoutConseilController.java`
```java
// Ligne 33: Declaration ComboBox
@FXML
private ComboBox<Analyse> analyseComboBox;

// Lignes 83-132: Chargement et affichage personnalise
private void loadAnalysesComboBox() {
    analysesList.addAll(analyseService.selectAll());
    analyseComboBox.setItems(analysesList);
    
    // Affichage: "ID: X - Date (Ferme: Y)"
    analyseComboBox.setCellFactory(param -> new ListCell<Analyse>() {
        @Override
        protected void updateItem(Analyse analyse, boolean empty) {
            setText(String.format("ID: %d - %s (Ferme: %d)",
                analyse.getIdAnalyse(),
                analyse.getDateAnalyse().format(formatter),
                analyse.getIdFerme()));
        }
    });
}

// Ligne 318: Recuperation FK lors de l'enregistrement
conseil.setIdAnalyse(analyseComboBox.getValue().getIdAnalyse());
```

**Service - Recuperation par FK**

**Fichier**: `ConseilService.java`
```java
// Lignes 105-118: Requete par FK
public List<Conseil> findByAnalyse(int idAnalyse) throws SQLException {
    String query = "SELECT * FROM conseil WHERE id_analyse = ? ORDER BY id_conseil DESC";
    try (PreparedStatement ps = cnx.prepareStatement(query)) {
        ps.setInt(1, idAnalyse);  // FK parameter binding
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            conseils.add(mapResultSetToConseil(rs));
        }
    }
    return conseils;
}
```

### 3.3 Affichage Dynamique (ResultSet vers ObservableList)

**Fichier**: `GestionAnalysesController.java`
```java
// Lignes 572-580: Chargement des donnees
private void loadAnalyses() {
    analysesList.clear();
    List<Analyse> analyses = analyseService.selectAll();  // ResultSet
    analysesList.addAll(analyses);                         // -> ObservableList
    analysesTableView.setItems(analysesList);              // -> TableView
    analysesTableView.refresh();
}

// Lignes 314-474: Configuration des colonnes
colId.setCellValueFactory(cellData -> 
    new SimpleObjectProperty<>(cellData.getValue().getIdAnalyse()));
colDate.setCellValueFactory(cellData -> 
    new SimpleStringProperty(cellData.getValue().getDateAnalyse().format(formatter)));
```

**Fichier**: `GestionConseilsController.java`
```java
// Lignes 91-170: CellValueFactory pour chaque colonne
colId.setCellValueFactory(cellData ->
    new SimpleObjectProperty<>(cellData.getValue().getIdConseil()));
colDescription.setCellValueFactory(cellData ->
    new SimpleStringProperty(cellData.getValue().getDescriptionConseil()));
colAnalyseId.setCellValueFactory(cellData ->   // Affichage FK
    new SimpleObjectProperty<>(cellData.getValue().getIdAnalyse()));
```

### 3.4 Controle de Saisie (Validation)

**Fichier**: `AnalyseDialog.java`
```java
// Lignes 180-244: Methode validateFields()
private boolean validateFields(DatePicker datePicker, TextField resultatField, ...) {
    boolean isValid = true;

    // Validation date
    if (datePicker.getValue() == null) {
        showError(dateErrorLabel);
        isValid = false;
    } else if (datePicker.getValue().isAfter(LocalDate.now())) {
        dateErrorLabel.setText("La date ne peut pas etre dans le futur");
        isValid = false;
    }

    // Validation resultat (min 5 caracteres)
    String resultat = resultatField.getText().trim();
    if (resultat.isEmpty() || resultat.length() < 5) {
        resultatErrorLabel.setText("Le resultat doit contenir au moins 5 caracteres");
        resultatField.setStyle("-fx-border-color: #D32F2F;");
        isValid = false;
    }

    // Validation IDs
    if (idTechnicienSpinner.getValue() <= 0) {
        showError(technicienErrorLabel);
        isValid = false;
    }

    return isValid;
}
```

**Fichier**: `AjoutConseilController.java`
```java
// Lignes 226-256: Validation avant appel service
private boolean validateFields() {
    boolean isValid = true;

    // Validation Analyse selection
    if (analyseComboBox.getValue() == null) {
        showError(analyseErrorLabel);
        isValid = false;
    }

    // Validation Priorite
    if (prioriteComboBox.getValue() == null) {
        showError(prioriteErrorLabel);
        isValid = false;
    }

    // Validation Description (min 10 caracteres)
    if (description == null || description.trim().length() < 10) {
        showError(descriptionErrorLabel);
        isValid = false;
    }

    return isValid;
}

// Ligne 297: Appel validation avant enregistrement
@FXML
private void handleEnregistrer() {
    if (!validateFields()) {
        NavigationUtil.showWarning("Validation", "Veuillez corriger les erreurs.");
        return;
    }
    // ... appel service
}
```

### 3.5 Stockage Image via URL (pas BLOB)

**Fichier**: `Analyse.java`
```java
// Ligne 16: Champ URL (String, pas BLOB)
private String imageUrl; // URL for visual documentation (no BLOB)

// Lignes 83-88: Getter/Setter
public String getImageUrl() {
    return imageUrl;
}

public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
}
```

**Fichier**: `farmai.sql`
```sql
-- Ligne 46: Definition colonne URL
image_url VARCHAR(255) DEFAULT NULL
```

**Fichier**: `GestionAnalysesController.java`
```java
// Lignes 438-471: Affichage image depuis URL
colImage.setCellFactory(col -> new TableCell<Analyse, String>() {
    @Override
    protected void updateItem(String imageUrl, boolean empty) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            ImageView imageView = new ImageView();
            imageView.setFitWidth(40);
            imageView.setFitHeight(30);
            
            File imageFile = new File(imageUrl);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                imageView.setImage(image);
            }
            setGraphic(imageView);
        }
    }
});
```

---

## 4. Base de Donnees

### Schema

**Base**: `farmai` (partagee avec Symfony)

```sql
-- Table: analyse
CREATE TABLE IF NOT EXISTS analyse (
    id_analyse INT(11) NOT NULL AUTO_INCREMENT,
    date_analyse TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resultat_technique TEXT DEFAULT NULL,
    id_technicien INT(11) NOT NULL,
    id_ferme INT(11) NOT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_analyse),
    FOREIGN KEY (id_technicien) REFERENCES user(id_user) ON DELETE CASCADE
);

-- Table: conseil (Relation 1:N avec analyse)
CREATE TABLE IF NOT EXISTS conseil (
    id_conseil INT(11) NOT NULL AUTO_INCREMENT,
    description_conseil TEXT NOT NULL,
    priorite ENUM('HAUTE', 'MOYENNE', 'BASSE') DEFAULT 'MOYENNE',
    id_analyse INT(11) NOT NULL,
    PRIMARY KEY (id_conseil),
    FOREIGN KEY (id_analyse) REFERENCES analyse(id_analyse) ON DELETE CASCADE
);
```

### Donnees de Test

| Table | Enregistrements |
|-------|-----------------|
| user | 4 utilisateurs (admin, expert, agricole, fournisseur) |
| analyse | 3 analyses de test |
| conseil | 4 conseils lies aux analyses |

---

## 5. Fonctionnalites Avancees (Bonus)

| US | Fonctionnalite | Fichier | Lignes |
|----|----------------|---------|--------|
| US8 | Diagnostic IA (Groq API) | `AnalyseService.java` | 110-130 |
| US9 | Export Rapport PDF/Texte | `PDFReportService.java` | 46-195 |
| US10 | Dashboard Statistiques | `StatisticsController.java` | 79-227 |

---

## 6. Checklist Validation

### Avant la Soutenance

- [ ] Base de donnees `farmai` creee et accessible
- [ ] Dependances Maven resolues
- [ ] Application se lance sans erreur
- [ ] Login fonctionne (expert@farmai.tn)
- [ ] TableView affiche les donnees
- [ ] CRUD Analyse fonctionne (ajouter, modifier, supprimer)
- [ ] CRUD Conseil fonctionne avec ComboBox 1:N
- [ ] Validation des champs actives
- [ ] Images s'affichent depuis URL

### Points Cles a Demontrer

1. **Singleton**: Ouvrir `MyDBConnexion.java` ligne 29
2. **PreparedStatement**: Ouvrir `AnalyseService.java` ligne 43
3. **1:N ComboBox**: Ouvrir `AjoutConseilController.java` ligne 33
4. **ObservableList**: Ouvrir `GestionAnalysesController.java` ligne 572
5. **Validation**: Ouvrir `AnalyseDialog.java` ligne 180
6. **Image URL**: Ouvrir `Analyse.java` ligne 16

---

## 7. Dependances Maven (pom.xml)

```xml
<!-- MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
</dependency>

<!-- JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.6</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>17.0.6</version>
</dependency>
```

---

*Document genere pour la validation CRUD - Module FarmIA Expert Conseil*
*Derniere mise a jour: Session 7*
