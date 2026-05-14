# 📋 FarmAI - Documentation Technique Simplifiée

## 🎯 Objectif
Ce document prouve que le projet FarmAI respecte les contraintes techniques des ateliers:
- **Workshop-JavaFX** : Architecture MVC, FXML, TableView, Navigation
- **Workshop-Test-unitaire** : JUnit 5, annotations, assertions, cleanup
- **Presentation-JDBC** : Connexion DB, PreparedStatement, CRUD

---

## 1️⃣ Architecture MVC (Workshop-JavaFX)

| Couche | Package | Exemple |
|--------|---------|---------|
| **Model** | `tn.esprit.farmai.models` | `Analyse.java`, `User.java`, `Conseil.java` |
| **View** | `tn/esprit/farmai/views/` | `expert-dashboard.fxml`, `gestion-analyses.fxml` |
| **Controller** | `tn.esprit.farmai.controllers` | `ExpertDashboardController.java` |

### ✅ Preuve de conformité

```java
// Model: Analyse.java (Ligne 8-20)
public class Analyse {
    private int idAnalyse;
    private LocalDateTime dateAnalyse;
    private String resultatTechnique;
    private int idTechnicien;  // FK to User
    private int idFerme;       // FK to Ferme
    private String imageUrl;   // URL (String), PAS de BLOB
}
```

```java
// Controller: ExpertDashboardController.java (Ligne 28-35)
@FXML private Label welcomeLabel;
@FXML private Label totalAnalysesLabel;
@FXML private TableView<Analyse> analysesTableView;
```

---

## 2️⃣ JDBC & PreparedStatement (Presentation-JDBC)

### ✅ Connexion à la Base de Données
```java
// MyDBConnexion.java (Ligne 7-12) - Pattern Singleton
private static final String URL = "jdbc:mysql://localhost:3306/farmai";
private static final String USERNAME = "root";
private static final String PASSWORD = "";

private MyDBConnexion() {
    cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
}
```

### ✅ PreparedStatement (Sécurité)
```java
// AnalyseService.java (Ligne 35-48) - Insert avec PreparedStatement
String query = "INSERT INTO analyse (date_analyse, resultat_technique, id_technicien, id_ferme, image_url) VALUES (?, ?, ?, ?, ?)";

try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
    ps.setTimestamp(1, Timestamp.valueOf(analyse.getDateAnalyse()));
    ps.setString(2, analyse.getResultatTechnique());
    ps.setInt(3, analyse.getIdTechnicien());
    ps.setInt(4, analyse.getIdFerme());
    ps.setString(5, analyse.getImageUrl()); // VARCHAR, PAS de BLOB
    ps.executeUpdate();
}
```

### ✅ Interface CRUD
```java
// CRUD.java - Interface standardisée
public interface CRUD<T> {
    void insertOne(T t) throws SQLException;
    void updateOne(T t) throws SQLException;
    void deleteOne(T t) throws SQLException;
    List<T> selectALL() throws SQLException;
}
```

---

## 3️⃣ JavaFX TableView & ObservableList (Workshop-JavaFX)

### ✅ TableView avec Colonnes
```java
// GestionAnalysesController.java (Ligne 50-75)
@FXML private TableView<Analyse> analysesTableView;
@FXML private TableColumn<Analyse, Integer> colId;
@FXML private TableColumn<Analyse, String> colDate;
@FXML private TableColumn<Analyse, String> colResultat;

// Setup des colonnes avec PropertyValueFactory
colId.setCellValueFactory(cellData -> 
    new SimpleObjectProperty<>(cellData.getValue().getIdAnalyse()));
```

### ✅ ObservableList pour mises à jour temps réel
```java
// GestionConseilsController.java (Ligne 45-50)
private ObservableList<Conseil> conseilsList;

public GestionConseilsController() {
    this.conseilsList = FXCollections.observableArrayList();
}
```

---

## 4️⃣ Navigation & FXML (Workshop-JavaFX)

### ✅ Chargement FXML
```java
// ExpertDashboardController.java (Ligne 95-110)
private void navigateWithFade(String fxmlPath, String title) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
    Parent newRoot = loader.load();
    Scene scene = new Scene(newRoot, 1200, 800);
    stage.setScene(scene);
}
```

### ✅ Structure FXML
```
src/main/resources/tn/esprit/farmai/views/
├── expert-dashboard.fxml      # Dashboard Expert
├── gestion-analyses.fxml      # Gestion des analyses
├── gestion-conseils.fxml      # Gestion des conseils
└── statistics.fxml            # Statistiques (US10)
```

---

## 5️⃣ Tests Unitaires JUnit 5 (Workshop-Test-unitaire)

### ✅ Annotations & Assertions
```java
// UserServicesTest.java (Ligne 15-28)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServicesTest {
    
    @BeforeAll
    public static void setup() {
        userService = new UserService();
    }

    @AfterEach
    public void cleanup() throws SQLException {
        if (testUser != null) {
            userService.deleteById(testUser.getIdUser());
        }
    }
}
```

### ✅ Tests CRUD
```java
// UserServicesTest.java (Ligne 32-48) - Test Ajouter
@Test
@Order(1)
public void testAjouter() throws SQLException {
    testUser = new User("TestNom", "TestPrenom", "test@example.com", ...);
    userService.insertOne(testUser);
    
    assertTrue(testUser.getIdUser() > 0, "User ID should be generated");
    assertFalse(users.isEmpty(), "User list should not be empty");
}
```

```java
// UserServicesTest.java (Ligne 52-65) - Test Modifier
@Test
@Order(2)
public void testModifier() throws SQLException {
    testUser.setNom("ModifiedNom");
    userService.updateOne(testUser);
    
    assertEquals("ModifiedNom", updatedUser.getNom(), "Last name should be updated");
}
```

```java
// UserServicesTest.java (Ligne 70-82) - Test Supprimer
@Test
@Order(3)
public void testSupprimer() throws SQLException {
    userService.deleteById(userId);
    
    assertFalse(deletedUser.isPresent(), "User should be deleted");
}
```

---

## 6️⃣ Alertes & Dialogs (Workshop-JavaFX)

### ✅ Alert Information
```java
// GestionAnalysesController.java (Ligne 320-325)
private void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
```

### ✅ Confirmation Dialog
```java
// GestionConseilsController.java (Ligne 195-200)
Optional<ButtonType> result = NavigationUtil.showConfirmation(
    "Confirmer la suppression",
    "Êtes-vous sûr de vouloir supprimer?"
);
if (result.isPresent() && result.get() == ButtonType.OK) {
    conseilService.deleteOne(conseil);
}
```

---

## 📊 Résumé de Conformité

| Contrainte | Fichier | Statut |
|------------|---------|--------|
| **MVC Architecture** | Models/Views/Controllers | ✅ |
| **FXML Views** | `*.fxml` files | ✅ |
| **TableView + ObservableList** | `GestionAnalysesController.java` | ✅ |
| **JDBC Connection** | `MyDBConnexion.java` | ✅ |
| **PreparedStatement** | `AnalyseService.java` | ✅ |
| **CRUD Interface** | `CRUD.java` | ✅ |
| **JUnit 5 Tests** | `UserServicesTest.java` | ✅ |
| **@BeforeAll/@AfterEach** | `UserServicesTest.java` | ✅ |
| **Assertions** | `assertTrue`, `assertEquals`, `assertFalse` | ✅ |
| **Alerts/Dialogs** | `AlertUtils.java`, Controllers | ✅ |
| **Images en URL (String)** | `Analyse.imageUrl` | ✅ |

---

## 📁 Structure des Fichiers Principaux

```
src/main/java/tn/esprit/farmai/
├── controllers/
│   ├── ExpertDashboardController.java    # Dashboard avec navigation
│   ├── GestionAnalysesController.java    # CRUD Analyses + US8/US9
│   └── GestionConseilsController.java    # CRUD Conseils + TTS
├── models/
│   ├── Analyse.java                      # Entité (MVC Model)
│   ├── Conseil.java                      # Entité avec Priorite
│   └── User.java                         # Entité User
├── services/
│   ├── AnalyseService.java               # CRUD + AI + PDF
│   ├── ConseilService.java               # CRUD Conseils
│   └── UserService.java                  # CRUD Users
├── interfaces/
│   └── CRUD.java                         # Interface standardisée
├── utils/
│   ├── MyDBConnexion.java                # Connexion JDBC (Singleton)
│   └── NavigationUtil.java               # Navigation JavaFX
└── test/
    └── UserServicesTest.java             # Tests JUnit 5

src/main/resources/tn/esprit/farmai/views/
├── expert-dashboard.fxml                 # Vue FXML Expert
├── gestion-analyses.fxml                 # Vue FXML Analyses
└── gestion-conseils.fxml                 # Vue FXML Conseils
```

---

*Dernière mise à jour : 01/03/2026*
*Projet Sprint Java - FarmAI*