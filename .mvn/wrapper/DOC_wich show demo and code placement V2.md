# FarmAI - Conformité Technique (V2)

## ✅ Workshop-JavaFX

| Contrainte | Implémentation | Fichier |
|------------|----------------|---------|
| MVC | `models/` + `views/` + `controllers/` | Structure projet |
| FXML | `expert-dashboard.fxml` | `src/main/resources/tn/esprit/farmai/views/` |
| TableView | `TableView<Analyse>` | `GestionAnalysesController.java:50` |
| ObservableList | `FXCollections.observableArrayList()` | `GestionConseilsController.java:45` |
| Navigation | `FXMLLoader.load(...)` | `ExpertDashboardController.java:95` |
| Alerts | `Alert(AlertType.INFORMATION)` | `GestionAnalysesController.java:320` |

## ✅ Presentation-JDBC

| Contrainte | Pourquoi ? | Fichier |
|------------|------------|---------|
| **Singleton DB** | Une seule connexion partagée → économie ressources, évite conflits | `MyDBConnexion.java:18-25` |

**Implémentation Singleton:** `src/main/java/tn/esprit/farmai/utils/MyDBConnexion.java`
```java
// Ligne 18: Variable statique pour stocker l'unique instance
private static MyDBConnexion instance;
private Connection cnx;

// Ligne 20-24: Constructeur privé - empêche "new MyDBConnexion()"
private MyDBConnexion() {
    cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
}

// Ligne 26-29: Point d'accès global
public static MyDBConnexion getInstance() {
    if (instance == null)
        instance = new MyDBConnexion();
    return instance;
}

// Usage dans Services: MyDBConnexion.getInstance().getCnx()
```
| **PreparedStatement** | Protection contre injections SQL + requêtes précompilées (+ rapide) | `AnalyseService.java:35-48` |

**Implémentation PreparedStatement:** `src/main/java/tn/esprit/farmai/services/AnalyseService.java`
```java
// Ligne 35-48: Méthode insertOne avec PreparedStatement
// Au lieu de: "INSERT INTO analyse VALUES (" + valeur + ")"  ← DANGEREUX (injection)
String query = "INSERT INTO analyse (date_analyse, resultat_technique, id_technicien, id_ferme, image_url) VALUES (?, ?, ?, ?, ?)";

PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
ps.setTimestamp(1, Timestamp.valueOf(analyse.getDateAnalyse()));   // Paramètre 1
ps.setString(2, analyse.getResultatTechnique());                   // Paramètre 2
ps.setInt(3, analyse.getIdTechnicien());                           // Paramètre 3
ps.setInt(4, analyse.getIdFerme());                                // Paramètre 4
ps.setString(5, analyse.getImageUrl());                            // Paramètre 5
ps.executeUpdate();  // Exécution sécurisée
```
| **CRUD Interface** | Standardisation des opérations de base, code réutilisable | `interfaces/CRUD.java` |

## ✅ Workshop-Test-unitaire

| Contrainte | Pourquoi ? | Fichier |
|------------|------------|---------|
| **@BeforeAll** | Initialiser le service une fois avant tous les tests | `UserServicesTest.java:17` |
| **@AfterEach** | Nettoyer la BD après chaque test → tests isolés | `UserServicesTest.java:22` |
| **@Test @Order** | Ordonner: Ajouter→Modifier→Supprimer | `UserServicesTest.java:30,45,60` |
| **Assertions** | Vérifier automatiquement les résultats | Tout le fichier |

## 🤖 APIs & Fonctionnalités Avancées

| Fonction | Type | Fichier | Usage |
|----------|------|---------|-------|
| **Groq AI** | API REST (externe) | `AnalyseService.java:75-140` | Diagnostics automatiques (US8) |
| **Weather** | API REST (Open-Meteo) | `WeatherUtils.java:60-95` | Enrichissement météo |
| **PDF** | Library locale (PDFBox) | `AnalyseService.java:220-330` | Rapports techniques (US9) |
| **Statistics** | JavaFX Charts | `StatisticsController.java:55-100` | Data visualization (US10) |

### Groq AI (US8 - Diagnostics)
```java
// AnalyseService.java:75-95 → API externe pour diagnostics IA
String apiKey = Config.GROQ_API_KEY;           // Clé dans config.properties
String jsonBody = buildGroqRequest(systemContent, userContent);
String response = SimpleHttpClient.postJson(Config.GROQ_API_URL, jsonBody, "Bearer " + apiKey);
return extractContent(response);  // Retourne le diagnostic généré
```

### Weather (Enrichissement)
```java
// WeatherUtils.java:60-80 → API Open-Meteo (gratuite, sans clé)
public static WeatherData fetchWeather(String lieu) {
    Coordinates coords = getCoordinates(cityName);  // Géocodage
    return getWeatherData(coords, cityName);        // Données météo
}
// AnalyseService.java:480 - Usage: enrichWithWeather(analyse, fermeLieu)
```

### PDF (US9 - Rapports)
```java
// AnalyseService.java:220-330 → Library Apache PDFBox (locale, pas API)
// Pourquoi locale? PDF généré côté serveur, pas d'appel réseau
PDDocument document = new PDDocument();
PDPage page = new PDPage(PDRectangle.A4);
contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
contentStream.showText("FARMIA TECHNICAL ANALYSIS REPORT");
// Ajoute: métadonnées, résultat technique, image, recommandations
document.save(filePath);  // Fichier binaire .pdf
```

### Statistics (US10 - Visualisation)
```java
// StatisticsController.java:55-100 → JavaFX Charts natifs
@FXML private PieChart priorityPieChart;    // Distribution priorités
@FXML private BarChart<String, Number> farmBarChart;  // Analyses/ferme
// Données via: AnalyseService.getConseilPriorityStats(), getAnalysisPerFarmStats()
```

## 📁 Placement du Code

| Couche | Dossier | Rôle |
|--------|---------|------|
| **Model** | `src/main/java/.../models/` | Entités (Analyse, User, Conseil) |
| **View** | `src/main/resources/.../views/` | Fichiers FXML |
| **Controller** | `src/main/java/.../controllers/` | Logique UI |
| **Service** | `src/main/java/.../services/` | Accès données + APIs |
| **Utils** | `src/main/java/.../utils/` | Outils (DB, Weather, TTS) |
| **Test** | `src/test/java/.../test/` | Tests JUnit 5 |

## 🔍 Détails Techniques

| Élément | Valeur | Preuve |
|---------|--------|--------|
| Images | URL String (pas BLOB) | `Analyse.java:15` |
| DB URL | `jdbc:mysql://localhost:3306/farmai` | `MyDBConnexion.java:9` |
| Groq API | `https://api.groq.com/openai/v1/chat/completions` | `Config.java` |
| Weather API | `https://geocoding-api.open-meteo.com` | `WeatherUtils.java:20` |
| PDF Library | `org.apache.pdfbox:pdfbox:2.0.29` | `pom.xml` |

---
*01/03/2026 - FarmAI Sprint Java*