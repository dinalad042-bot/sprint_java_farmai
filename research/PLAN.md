# Implementation Plan: Expert Module Enhancement

## Overview
Add 2 advanced functionalities to the Expert module using Groq API vision capabilities, following YAGNI/KISS/SRP/FOSD principles.

## Research Summary
Based on **6 research areas** investigated. Full research at:
- [14-expert-module-analysis.md](areas/14-expert-module-analysis.md) — Current expert features: chatbot, voice, analyses, conseils
- [15-agricole-features-gap.md](areas/15-agricole-features-gap.md) — Agricole has weather, irrigation AI, market export
- [16-groq-api-vision.md](areas/16-groq-api-vision.md) — Groq supports llama-3.2-11b/90b-vision models
- [17-principles-audit.md](areas/17-principles-audit.md) — YAGNI/KISS/SRP/FOSD violations identified
- [18-feature-design.md](areas/18-feature-design.md) — 2 feature designs validated against principles

## Current State Analysis

### Existing Expert Infrastructure
| Component | Location | Capability |
|-----------|----------|------------|
| Groq API Integration | `ExpertChatbotService.java:1-73` | Text-only, llama-3.3-70b |
| Image Storage | `Analyse.imageUrl:1-97` | URL-based (not BLOB) |
| PDF Generation | `AnalyseService.exportAnalysisToPDF():236-334` | Apache PDFBox |
| Analysis CRUD | `AnalyseService.java:1-573` | Full CRUD with weather enrichment |

### Gap Identified
- **No image analysis capability** — Expert module cannot analyze plant images for disease diagnosis
- **Unused weather enrichment** — `AnalyseService.enrichWithWeather()` exists but not exposed in UI
- **HTML export orphan** — `exportAnalysisToHTML()` unused (YAGNI violation)

### Principles Audit Findings
| Violation | Location | Action Required |
|-----------|----------|-----------------|
| YAGNI - HTML Export | `AnalyseService:403-473` | Remove unused method |
| SRP - AnalyseService too big | `AnalyseService.java` | Split into services |
| KISS - Complex error handling | `AnalyseService:96-145` | Simplify with JSON parsing |
| FOSD - Orphan weather feature | `AnalyseService:516-571` | Activate with UI checkbox |

## Desired End State

### Feature 1: AI Visual Plant Disease Diagnosis
Experts can upload plant images and receive AI-powered disease diagnosis using Groq vision models.

**Success Criteria**:
- [ ] New "Visual Diagnose" button in GestionAnalyses view
- [ ] File chooser opens for image selection (JPG/PNG)
- [ ] Image base64 encoded and sent to Groq vision API
- [ ] Diagnosis displayed with: condition, confidence, symptoms, treatment, prevention
- [ ] Result can be saved to analysis record

### Feature 2: Intelligent Analysis Report Generator
Experts can generate comprehensive PDF reports combining analysis data, weather conditions, and recommendations.

**Success Criteria**:
- [ ] New "Generate Report" button in ExpertDashboard
- [ ] Dialog for farm selection and date range
- [ ] Report includes: summary, weather context, analysis history, recommendations
- [ ] AI-generated executive summary using Groq
- [ ] PDF opens automatically after generation

## Out of Scope
- HTML export (YAGNI - removing existing unused code)
- Mobile app support
- Real-time notifications
- Multi-language support (French only)
- Video analysis

## Implementation Phases

### Phase 0: Cleanup & Refactoring (Prerequisites)
**Goal**: Address YAGNI/KISS/SRP violations before adding features
**Research basis**: [17-principles-audit.md](areas/17-principles-audit.md)

**Changes**:

| File | Change | Why |
|------|--------|-----|
| `AnalyseService.java:403-473` | **Remove** `exportAnalysisToHTML()` | YAGNI - unused code |
| `AnalyseService.java:147-159` | Refactor `buildGroqRequest()` to use org.json | Standards compliance |
| `AnalyseService.java:96-145` | Simplify error handling with JSON parsing | KISS principle |

**Detailed changes**:
```java
// BEFORE (string concatenation)
return "{" +
    "\"model\":\"" + Config.GROQ_MODEL + "\"," +
    "\"messages\":[...]";

// AFTER (org.json)
JSONObject request = new JSONObject();
request.put("model", Config.GROQ_MODEL);
request.put("messages", messagesArray);
return request.toString();
```

**Success criteria**:
- [ ] `mvnw clean compile` succeeds
- [ ] No references to HTML export in codebase
- [ ] Existing tests still pass

**Depends on**: Nothing

---

### Phase 1: Visual Plant Disease Diagnosis
**Goal**: Implement AI image analysis for plant disease diagnosis
**Research basis**: [16-groq-api-vision.md](areas/16-groq-api-vision.md), [18-feature-design.md](areas/18-feature-design.md)

**New Files**:

| File | Purpose |
|------|---------|
| `models/DiagnosisResult.java` | Data model for diagnosis results |
| `services/ExpertVisionService.java` | Groq vision API integration |
| `exceptions/VisionException.java` | Custom exception for vision errors |

**Modified Files**:

| File | Change | Why |
|------|--------|-----|
| `GestionAnalysesController.java:78-195` | Add `handleVisualDiagnosis()` method | UI handler |
| `GestionAnalysesController.java` | Add "Visual Diagnose" button | User interaction |
| `gestion-analyses.fxml` | Add button to FXML | Layout |
| `Config.java` | Add `GROQ_VISION_MODEL` constant | Configuration |

**Detailed changes**:

**1. DiagnosisResult.java** (New Model):
```java
package tn.esprit.farmai.models;

public class DiagnosisResult {
    public enum ConfidenceLevel { HIGH, MEDIUM, LOW }
    
    private String condition;
    private ConfidenceLevel confidence;
    private String symptoms;
    private String treatment;
    private String prevention;
    private String urgency;
    private boolean needsExpertConsult;
    
    // Getters, setters, constructor
}
```

**2. ExpertVisionService.java** (New Service):
```java
package tn.esprit.farmai.services;

import org.json.*;
import java.util.Base64;
import java.nio.file.*;

public class ExpertVisionService {
    private static final String VISION_MODEL = "llama-3.2-11b-vision-preview";
    private static final int MAX_IMAGE_SIZE_MB = 5;
    
    public DiagnosisResult analyzePlantImage(String imagePath) throws VisionException {
        // 1. Validate image
        validateImage(imagePath);
        
        // 2. Encode to base64
        String base64Image = encodeImageToBase64(imagePath);
        String dataUri = "data:image/jpeg;base64," + base64Image;
        
        // 3. Build request
        JSONObject request = buildVisionRequest(dataUri);
        
        // 4. Call API
        String response = SimpleHttpClient.postJson(
            Config.GROQ_API_URL, 
            request.toString(), 
            "Bearer " + Config.GROQ_API_KEY
        );
        
        // 5. Parse response
        return parseDiagnosisResponse(response);
    }
    
    private String encodeImageToBase64(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    private JSONObject buildVisionRequest(String dataUri) {
        JSONObject request = new JSONObject();
        request.put("model", VISION_MODEL);
        request.put("max_tokens", 800);
        request.put("temperature", 0.3);
        
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        
        JSONArray content = new JSONArray();
        content.put(new JSONObject()
            .put("type", "text")
            .put("text", getSystemPrompt()));
        content.put(new JSONObject()
            .put("type", "image_url")
            .put("image_url", new JSONObject().put("url", dataUri)));
        
        message.put("content", content);
        messages.put(message);
        request.put("messages", messages);
        
        return request;
    }
    
    private String getSystemPrompt() {
        return """
            You are an expert agricultural pathologist specializing in Tunisian crops.
            Analyze the plant image and provide a diagnosis in this exact format:
            
            CONDITION: [Disease name or "Healthy Plant"]
            CONFIDENCE: [High/Medium/Low]
            SYMPTOMS: [Description of visible symptoms]
            TREATMENT: [Treatment recommendations]
            PREVENTION: [Prevention measures]
            URGENCY: [Immediate/Within week/Monitor]
            EXPERT_CONSULT: [Yes/No]
            
            Respond in French.
            """;
    }
}
```

**3. GestionAnalysesController.java** (Add handler):
```java
@FXML
private void handleVisualDiagnosis() {
    // File chooser for image
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Plant Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png")
    );
    
    File selectedFile = fileChooser.showOpenDialog(getCurrentStage());
    if (selectedFile == null) return;
    
    // Show loading dialog
    ProgressDialog.show("Analyzing image...");
    
    // Run in background thread
    new Thread(() -> {
        try {
            ExpertVisionService visionService = new ExpertVisionService();
            DiagnosisResult result = visionService.analyzePlantImage(
                selectedFile.getAbsolutePath()
            );
            
            Platform.runLater(() -> {
                ProgressDialog.close();
                showDiagnosisResultDialog(result);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                ProgressDialog.close();
                showError("Analysis Error", e.getMessage());
            });
        }
    }).start();
}
```

**Success criteria**:

**Automated**:
- [ ] `mvnw clean compile` succeeds
- [ ] `mvnw test` passes (if tests exist)

**Manual**:
- [ ] Open GestionAnalyses view
- [ ] Click "Visual Diagnose" button
- [ ] Select a plant image (JPG/PNG)
- [ ] Receive diagnosis with condition, confidence, treatment
- [ ] Can save diagnosis to analysis record

**Depends on**: Phase 0

---

### Phase 2: Intelligent Report Generator
**Goal**: Generate comprehensive PDF reports with AI summary
**Research basis**: [18-feature-design.md](areas/18-feature-design.md)

**New Files**:

| File | Purpose |
|------|---------|
| `services/IntelligentReportService.java` | Report generation orchestration |
| `models/ReportConfig.java` | Report configuration model |
| `utils/DateRange.java` | Date range utility |

**Modified Files**:

| File | Change | Why |
|------|--------|-----|
| `ExpertDashboardController.java` | Add `handleGenerateReport()` method | UI handler |
| `expert-dashboard.fxml` | Add "Generate Report" button | Layout |
| `AnalyseService.java` | Move PDF logic to new service | SRP compliance |

**Detailed changes**:

**1. IntelligentReportService.java** (New Service):
```java
package tn.esprit.farmai.services;

import org.apache.pdfbox.pdmodel.*;
import org.json.JSONObject;

public class IntelligentReportService {
    
    public String generateFarmReport(int fermeId, DateRange range) throws ReportException {
        // 1. Fetch data
        Ferme ferme = fermeService.findById(fermeId);
        List<Analyse> analyses = analyseService.findByFermeAndDateRange(fermeId, range);
        List<Conseil> conseils = conseilService.findByFerme(fermeId);
        
        // 2. Generate AI executive summary
        String executiveSummary = generateExecutiveSummary(analyses, conseils);
        
        // 3. Build PDF
        String filePath = buildPdfReport(ferme, analyses, conseils, executiveSummary);
        
        return filePath;
    }
    
    private String generateExecutiveSummary(List<Analyse> analyses, List<Conseil> conseils) {
        // Build prompt from data
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this farm data and generate an executive summary:\n\n");
        prompt.append("Analyses: ").append(analyses.size()).append("\n");
        prompt.append("Recommendations: ").append(conseils.size()).append("\n");
        
        // Call Groq API
        JSONObject request = new JSONObject();
        request.put("model", Config.GROQ_MODEL);
        request.put("max_tokens", 500);
        request.put("messages", new JSONArray()
            .put(new JSONObject()
                .put("role", "system")
                .put("content", "You are an agricultural report writer. Be concise and professional."))
            .put(new JSONObject()
                .put("role", "user")
                .put("content", prompt.toString())));
        
        // Parse and return summary
        return callGroqAPI(request);
    }
}
```

**2. ExpertDashboardController.java** (Add handler):
```java
@FXML
private void handleGenerateReport() {
    // Show dialog for farm selection and date range
    ReportDialog dialog = new ReportDialog();
    Optional<ReportConfig> result = dialog.showAndWait();
    
    result.ifPresent(config -> {
        ProgressDialog.show("Generating report...");
        
        new Thread(() -> {
            try {
                IntelligentReportService reportService = new IntelligentReportService();
                String pdfPath = reportService.generateFarmReport(
                    config.getFermeId(), 
                    config.getDateRange()
                );
                
                Platform.runLater(() -> {
                    ProgressDialog.close();
                    openPdf(pdfPath);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ProgressDialog.close();
                    showError("Report Error", e.getMessage());
                });
            }
        }).start();
    });
}
```

**Success criteria**:

**Automated**:
- [ ] `mvnw clean compile` succeeds
- [ ] `mvnw test` passes

**Manual**:
- [ ] Open Expert Dashboard
- [ ] Click "Generate Report" button
- [ ] Select farm and date range
- [ ] Report generates with all sections
- [ ] AI executive summary included
- [ ] PDF opens automatically

**Depends on**: Phase 1

---

## Testing Strategy

### Unit Tests (If Testing Framework Available)
1. `ExpertVisionServiceTest.testAnalyzePlantImage()`
2. `ExpertVisionServiceTest.testImageEncoding()`
3. `IntelligentReportServiceTest.testReportGeneration()`

### Manual Testing Checklist
- [ ] Visual diagnosis with healthy plant image
- [ ] Visual diagnosis with diseased plant image
- [ ] Visual diagnosis with invalid file type (should error gracefully)
- [ ] Report generation with no analyses (empty state)
- [ ] Report generation with many analyses (performance)
- [ ] Report generation with date range filter

## Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Groq API quota exceeded | Low | High | Implement usage tracking; cache results |
| Image files too large | Medium | Medium | Resize images before encoding |
| PDF generation slow | Medium | Medium | Async generation with progress indicator |
| Vision API inaccurate | Medium | Medium | Display confidence level; flag for human review |
| Breaking existing features | Low | High | Comprehensive testing; feature flags |

## All Files Referenced

### Existing Files Modified
- `src/main/java/tn/esprit/farmai/services/AnalyseService.java`
- `src/main/java/tn/esprit/farmai/controllers/GestionAnalysesController.java`
- `src/main/java/tn/esprit/farmai/controllers/ExpertDashboardController.java`
- `src/main/java/tn/esprit/farmai/utils/Config.java`
- `src/main/resources/tn/esprit/farmai/views/gestion-analyses.fxml`
- `src/main/resources/tn/esprit/farmai/views/expert-dashboard.fxml`

### New Files Created
- `src/main/java/tn/esprit/farmai/models/DiagnosisResult.java`
- `src/main/java/tn/esprit/farmai/services/ExpertVisionService.java`
- `src/main/java/tn/esprit/farmai/services/IntelligentReportService.java`
- `src/main/java/tn/esprit/farmai/models/ReportConfig.java`
- `src/main/java/tn/esprit/farmai/utils/DateRange.java`
- `src/main/java/tn/esprit/farmai/exceptions/VisionException.java`

## Documentation References

1. **Groq Vision API**: https://console.groq.com/docs/vision
2. **Groq Models**: https://console.groq.com/docs/models
3. **Apache PDFBox**: https://pdfbox.apache.org/2.0/getting-started.html
4. **Java Base64**: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Base64.html
5. **OpenAI Error Handling**: https://platform.openai.com/docs/guides/error-codes
6. **Clean Code - SRP**: https://blog.cleancoder.com/uncle-bob/2014/05/08/SingleReponsibilityPrinciple.html

---

## Summary

This plan delivers 2 advanced expert functionalities:
1. **Visual Plant Disease Diagnosis** — Leverages Groq vision API for AI-powered image analysis
2. **Intelligent Report Generator** — Combines analysis data with AI-generated insights

Both features follow YAGNI/KISS/SRP/FOSD principles:
- **YAGNI**: No speculative features; removes unused HTML export
- **KISS**: Simple API calls, clear response parsing
- **SRP**: New services for vision and reports; slimmed-down AnalyseService
- **FOSD**: Clear user stories mapped to every feature

**Estimated Effort**: 3-4 days for experienced Java developer
**Dependencies**: Groq API key with vision model access
