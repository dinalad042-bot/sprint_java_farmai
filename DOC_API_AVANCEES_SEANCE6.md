# 🚀 Fonctionnalités API Avancées - Module Analyse & Conseil
## Séance 6 : Prototype & Intégration | FarmAI Sprint Java

---

## 📡 Vue d'ensemble des APIs Intégrées

| API | Fichier | US | Statut |
|-----|---------|-----|--------|
| **Groq AI** | `AnalyseService.java` | US8 | ✅ Opérationnel |
| **Open-Meteo** | `WeatherUtils.java` | Enrichissement | ✅ Opérationnel |
| **TTS (Speech)** | `SpeechUtils.java` | Accessibilité | ✅ Opérationnel |
| **PDF Generation** | `AnalyseService.java` | US9 | ✅ Opérationnel |

---

## 1️⃣ US8 : AI-Assisted Diagnostics (Groq API)

### 📍 Localisation du Code
```
src/main/java/tn/esprit/farmai/services/AnalyseService.java
├── generateAIDiagnostic(String observation)  → Ligne 95-145
├── buildGroqRequest(...)                     → Ligne 147-158
└── extractContent(String response)           → Ligne 165-260
```

### 🔧 Configuration Requise
```properties
# Fichier: config.properties (à créer à la racine)
GROQ_API_KEY=gsk_your_key_here
GROQ_API_URL=https://api.groq.com/openai/v1/chat/completions
GROQ_MODEL=llama-3.1-8b-instant
```

### 📝 Signature de la Méthode
```java
/**
 * US8: AI-Assisted Diagnostics using Groq API
 * 
 * @param observation Raw observation data from technician
 * @return AI-generated technical analysis
 * @throws IOException if API call fails
 * 
 * Railway Track Trace: Targets Analyse.resultat_technique attribute
 */
public String generateAIDiagnostic(String observation) throws IOException, InterruptedException
```

### 🔄 Flux d'Exécution (Non-bloquant UI)
```
[Controller] handleAIDiagnostic()
      │
      ├─→ Dialog: Saisie observation
      │
      ├─→ new Thread(() -> {
      │       analyseService.generateAIDiagnostic(observation);
      │   }).start();
      │
      └─→ Platform.runLater(() -> {
              // Update UI with AI result
          });
```

### 📊 Exemple d'Appel
```java
// Dans GestionAnalysesController.java (Ligne 108-160)
new Thread(() -> {
    try {
        String aiResult = analyseService.generateAIDiagnostic(observation);
        
        Platform.runLater(() -> {
            // Afficher le résultat dans une TextArea
            TextArea resultArea = new TextArea(aiResult);
            // ... mise à jour UI
        });
        
    } catch (Exception e) {
        Platform.runLater(() -> {
            showAlert(Alert.AlertType.ERROR, "AI Error", e.getMessage());
        });
    }
}).start();
```

### ⚠️ Gestion des Erreurs
| Erreur | Message Utilisateur |
|--------|---------------------|
| `invalid_api_key` | "Invalid Groq API key. Get a new key from https://console.groq.com/keys" |
| `insufficient_quota` | "Groq API quota exceeded. Check usage at https://console.groq.com/usage" |
| `rate limit` | "AI service rate limit reached. Please wait and try again." |
| `model_not_found` | "AI model not available. Check models at https://console.groq.com/docs/models" |

---

## 2️⃣ API Météo (Open-Meteo) - Enrichissement Automatique

### 📍 Localisation du Code
```
src/main/java/tn/esprit/farmai/utils/WeatherUtils.java
├── fetchWeather(String lieu)              → Ligne 60-95
├── getCoordinates(String cityName)        → Ligne 130-165
├── getWeatherData(Coordinates, String)    → Ligne 170-195
└── getWeatherDescription(int code)        → Ligne 230-255
```

### ✅ Avantages (KISS Principle)
- **Gratuit** : Pas de clé API requise
- **Sans inscription** : Appel direct
- **Timeouts configurés** : Connect 5s, Read 10s

### 📝 Signature
```java
/**
 * Fetch weather data for a given location.
 * Uses Open-Meteo API (free, no API key required).
 * 
 * @param lieu The farm location (e.g., "Tunis, Tunisie")
 * @return WeatherData record with temperature, humidity, conditions
 */
public static WeatherData fetchWeather(String lieu)
```

### 🔄 Intégration dans Analyse
```java
// Dans AnalyseService.java (Ligne 380-410)
public String enrichWithWeather(Analyse analyse, String fermeLieu) {
    try {
        WeatherUtils.WeatherData weather = WeatherUtils.fetchWeather(fermeLieu);
        
        if (weather.success()) {
            // Append weather to resultat_technique (KISS - no schema change)
            String weatherInfo = weather.formatForDiagnostic();
            // Result: "Météo: 25.5°C, Humidité: 65%, Ensoleillé"
            return analyse.getResultatTechnique() + " | " + weatherInfo;
        }
    } catch (Exception e) {
        // Graceful degradation - don't break analysis
        System.err.println("Weather API failed: " + e.getMessage());
    }
    return analyse.getResultatTechnique();
}
```

### 📊 Format de Sortie
```
Météo: 25.5°C, Humidité: 65%, Ciel dégagé
```

### 🗺️ Villes Tunisiennes Supportées
```java
// Mapping automatique dans normalizeCityName()
"Tunis" → Tunis
"Sfax" → Sfax
"Sousse" → Sousse
"Bizerte" → Bizerte
"Nabeul" → Nabeul
"Gabès/Gabes" → Gabes
```

---

## 3️⃣ Text-to-Speech (TTS) - Lecture des Conseils

### 📍 Localisation du Code
```
src/main/java/tn/esprit/farmai/utils/SpeechUtils.java
├── speakAsync(String text)              → Ligne 50-60 (CompletableFuture)
├── speak(String text)                   → Ligne 65-85
├── speakWithSystemTTS(String text)      → Ligne 100-135 (Windows/macOS/Linux)
└── speakWithApi(String text)            → Ligne 145-165 (Fallback)
```

### 📝 Signature
```java
/**
 * Speak text asynchronously using TTS API.
 * Non-blocking UI - uses CompletableFuture.
 * 
 * @param text The text to speak (Conseil description)
 * @return CompletableFuture<Void> for chaining/cancellation
 */
public static CompletableFuture<Void> speakAsync(String text)
```

### 🔄 Intégration dans GestionConseilsController
```java
// Dans GestionConseilsController.java (Ligne 290-320)
private void handleReadAloud(Conseil conseil, Button ttsBtn) {
    String description = conseil.getDescriptionConseil();
    
    // Update button state
    ttsBtn.setText("⏹");
    
    // Run TTS asynchronously to avoid UI freeze
    SpeechUtils.speakAsync(description)
        .thenRun(() -> {
            Platform.runLater(() -> {
                ttsBtn.setText("🔊"); // Reset when done
            });
        })
        .exceptionally(ex -> {
            Platform.runLater(() -> {
                AlertUtils.showError("Erreur TTS", ex.getMessage());
            });
            return null;
        });
}
```

### 🖥️ Support Multi-Plateforme
| OS | Méthode | Commande |
|----|---------|----------|
| Windows | PowerShell SAPI | `powershell -Command "$synth.Speak('text')"` |
| macOS | `say` command | `s ay -v Thomas "text"` |
| Linux | `espeak` | `espeak -v fr "text"` |

---

## 4️⃣ US9 : PDF Technical Reporting

### 📍 Localisation du Code
```
src/main/java/tn/esprit/farmai/services/AnalyseService.java
├── exportAnalysisToPDF(int idAnalyse)    → Ligne 265-380
├── drawSectionHeader(...)                 → Ligne 385-395
├── drawWrappedText(...)                   → Ligne 400-440
└── drawImageFromUrl(...)                  → Ligne 455-530
```

### 📦 Dépendance Maven
```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.29</version>
</dependency>
```

### 📝 Signature
```java
/**
 * US9: PDF Technical Report - generates real binary PDF
 * 
 * @param idAnalyse The analysis ID to export
 * @return Path to generated PDF report file
 * @throws SQLException if database error occurs
 * @throws IOException if file creation fails
 * 
 * Railway Track: Implements 1:N relationship (Analyse -> Conseil)
 */
public String exportAnalysisToPDF(int idAnalyse) throws SQLException, IOException
```

### 🔄 Flux Non-Bloquant (Background Thread)
```java
// Dans GestionAnalysesController.java (Ligne 170-210)
@FXML
private void handleExportPDF() {
    exportPdfButton.setDisable(true);
    
    new Thread(() -> {
        try {
            String pdfPath = analyseService.exportAnalysisToPDF(idAnalyse);
            
            Platform.runLater(() -> {
                exportPdfButton.setDisable(false);
                showPDFSuccessDialog(pdfPath);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "PDF Error", e.getMessage());
            });
        }
    }).start();
}
```

### 📄 Contenu du PDF Généré
```
┌─────────────────────────────────────────────┐
│     FARMIA TECHNICAL ANALYSIS REPORT        │
├─────────────────────────────────────────────┤
│ Analysis ID: #42                            │
│ Date: 25/02/2026 07:30                      │
│ Farm ID: 3  |  Technician ID: 5             │
├─────────────────────────────────────────────┤
│ TECHNICAL RESULT                            │
│ Detection of fungal infection in wheat...   │
│ Météo: 18°C, Humidité: 72%, Nuageux         │
├─────────────────────────────────────────────┤
│ ANALYSIS IMAGE                              │
│ [Image loaded from URL]                     │
├─────────────────────────────────────────────┤
│ RECOMMENDATIONS (2)                         │
│ 1. [HAUTE PRIORITY] Apply fungicide...      │
│ 2. [MOYENNE PRIORITY] Monitor humidity...   │
└─────────────────────────────────────────────┘
```

---

## 5️⃣ US10 : Dashboard Statistics (Data Visualization)

### 📍 Localisation du Code
```
src/main/java/tn/esprit/farmai/controllers/StatisticsController.java
├── loadPriorityStats()        → PieChart (Conseil priority distribution)
├── loadFarmAnalysisStats()    → BarChart (Analyses per farm)
└── loadOverviewStats()        → Summary labels
```

### 📊 Charts JavaFX
```java
// PieChart - Distribution des priorités
@FXML private PieChart priorityPieChart;

// BarChart - Analyses par ferme
@FXML private BarChart<String, Number> farmBarChart;
@FXML private CategoryAxis xAxis;
@FXML private NumberAxis yAxis;
```

### 📈 Requêtes SQL Utilisées
```java
// Dans AnalyseService.java
public List<Object[]> getConseilPriorityStats() {
    // SQL: SELECT priorite, COUNT(*) FROM conseil GROUP BY priorite
}

public List<Object[]> getAnalysisPerFarmStats() {
    // SQL: SELECT id_ferme, COUNT(*) FROM analyse GROUP BY id_ferme
}
```

---

## 🎯 Mapping FOSD : Bouton → User Story

| Bouton/Feature | User Story | Fichier | Ligne |
|----------------|------------|---------|-------|
| 🤖 **"AI Diagnostic"** | US8 | `GestionAnalysesController.java` | 85-160 |
| 📄 **"Export PDF"** | US9 | `GestionAnalysesController.java` | 170-210 |
| 📊 **"Statistiques"** | US10 | `StatisticsController.java` | 55-100 |
| 🔊 **"Lire (TTS)"** | Accessibilité | `GestionConseilsController.java` | 290-320 |
| 🌤️ **Enrichissement Météo** | Quality | `AnalyseService.java` | 380-410 |

---

## ⚡ Résumé - Appels API Asynchrones

| API | Méthode Async | Prévient UI Freeze |
|-----|---------------|-------------------|
| Groq AI | `new Thread(() -> {...}).start()` | ✅ |
| Weather | Timeout 5s/10s + Graceful degradation | ✅ |
| TTS | `CompletableFuture.runAsync()` | ✅ |
| PDF | `new Thread(() -> {...}).start()` | ✅ |

---

## ✅ Confirmation Finale

> **Images en URL (String), PAS en BLOB**
> 
> Preuve dans `Analyse.java` :
> ```java
> private String imageUrl; // URL for visual documentation (no BLOB)
> ```
> 
> Preuve dans `AnalyseService.java` :
> ```java
> ps.setString(5, analyse.getImageUrl()); // Stocké en VARCHAR
> ```

---

*Document généré pour la Séance 6 - Sprint Java FarmAI*
*Dernière mise à jour : 25/02/2026*