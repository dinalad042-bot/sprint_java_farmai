# Research Area: 2 Advanced Expert Features Design

## Status: 🟢 Complete

## Design Requirements
Based on research and user requirements:
1. Use Groq API with image analysis capabilities
2. Follow YAGNI/KISS/SRP/FOSD principles
3. Fill real gaps in expert functionality
4. Map to user requirements

## Proposed Features

### Feature 1: AI Visual Plant Disease Diagnosis

#### User Story
**US-EXPERT-V1**: As an agricultural expert, I want to upload plant images and receive AI-powered disease diagnosis so that I can quickly identify crop issues and provide accurate treatment recommendations.

#### Feature Description
Enable experts to analyze plant images using Groq's vision models to detect diseases, pests, and nutritional deficiencies.

#### Technical Design

**New Service**: `ExpertVisionService.java`
```java
package tn.esprit.farmai.services;

public class ExpertVisionService {
    private static final String VISION_MODEL = "llama-3.2-11b-vision-preview";
    
    /**
     * Analyze plant image for disease diagnosis
     * @param imagePath Path to local image file
     * @return DiagnosisResult with disease info and recommendations
     */
    public DiagnosisResult analyzePlantImage(String imagePath) throws VisionException;
    
    /**
     * Compare multiple images for progression analysis
     * @param imagePaths Array of image paths (time series)
     * @return ProgressionAnalysis with trend information
     */
    public ProgressionResult analyzeProgression(String[] imagePaths) throws VisionException;
}
```

**New Model**: `DiagnosisResult.java`
```java
package tn.esprit.farmai.models;

public class DiagnosisResult {
    private String condition;          // Disease name or "Healthy"
    private ConfidenceLevel confidence; // HIGH, MEDIUM, LOW
    private String description;        // Detailed symptoms
    private String treatment;          // Treatment steps
    private String prevention;         // Prevention measures
    private boolean needsExpertReview; // When to consult human
}
```

**UI Integration**: `GestionAnalysesController.java`
- Add "Visual Diagnose" button next to AI Diagnostic button
- Opens file chooser for image selection
- Displays diagnosis result in dialog with option to save to analysis

**API Request Format**:
```json
{
  "model": "llama-3.2-11b-vision-preview",
  "messages": [
    {
      "role": "system",
      "content": "You are an expert agricultural pathologist..."
    },
    {
      "role": "user",
      "content": [
        {"type": "text", "text": "Analyze this plant image for diseases."},
        {"type": "image_url", "image_url": {"url": "data:image/jpeg;base64,..."}}
      ]
    }
  ],
  "max_tokens": 800,
  "temperature": 0.3
}
```

**System Prompt**:
```
You are an expert agricultural pathologist specializing in Tunisian crops.
Analyze the plant image and provide a diagnosis in this exact format:

CONDITION: [Disease name or "Healthy Plant"]
CONFIDENCE: [High/Medium/Low]
SYMPTOMS: [Detailed description of visible symptoms]
TREATMENT: [Step-by-step treatment recommendations]
PREVENTION: [Prevention measures for future]
URGENCY: [Immediate/Within week/Monitor]
EXPERT_CONSULT: [Yes/No - whether human expert needed]

Be precise and practical for Tunisian agricultural conditions.
Respond in French.
```

**Error Handling**:
- Image too large → Resize and retry
- Invalid format → Convert to JPEG
- API error → Show user-friendly message
- Low confidence → Flag for human review

#### KISS Compliance
- Single method for image analysis
- Base64 encoding using standard Java library
- Clear response parsing with regex
- No complex retry logic

#### SRP Compliance
- `ExpertVisionService`: Only handles vision API calls
- `GestionAnalysesController`: Only handles UI
- `DiagnosisResult`: Pure data model

#### FOSD Compliance
- Clear user story: US-EXPERT-V1
- Directly addresses expert workflow
- Measurable value: faster diagnosis

---

### Feature 2: Intelligent Analysis Report Generator

#### User Story
**US-EXPERT-V2**: As an agricultural expert, I want to generate comprehensive reports that combine analysis data, weather conditions, and treatment recommendations so that I can provide agricole users with actionable insights for their farms.

#### Feature Description
Generate rich PDF reports that correlate:
- Analysis history for a farm
- Weather conditions at analysis time
- Expert conseils/recommendations
- Visual diagnosis results (from Feature 1)

#### Technical Design

**New Service**: `IntelligentReportService.java`
```java
package tn.esprit.farmai.services;

public class IntelligentReportService {
    
    /**
     * Generate comprehensive farm analysis report
     * @param fermeId Farm ID
     * @param dateRange Optional date range filter
     * @return Path to generated PDF report
     */
    public String generateFarmReport(int fermeId, DateRange range) throws ReportException;
    
    /**
     * Generate comparative analysis report (multiple farms)
     * @param fermeIds Array of farm IDs
     * @return Path to generated PDF report
     */
    public String generateComparativeReport(int[] fermeIds) throws ReportException;
}
```

**Report Structure**:
1. **Cover Page**: Farm name, report date, expert info
2. **Executive Summary**: Key findings, alert count, trend
3. **Weather Context**: Conditions during analyses
4. **Analysis History**: All analyses with visual thumbnails
5. **Diagnosis Summary**: Disease frequency, severity trends
6. **Recommendations**: All conseils grouped by priority
7. **Action Plan**: Prioritized next steps with AI suggestions

**AI Enhancement**: Use Groq to generate executive summary
```
System Prompt:
"Analyze the following farm analysis data and generate an executive summary 
highlighting key issues, trends, and urgent actions needed. Be concise and 
professional. Data: [analysis summaries]"
```

**UI Integration**: `ExpertDashboardController.java`
- Add "Generate Report" button
- Dialog for farm selection and date range
- Progress indicator during generation
- Open PDF on completion

**Database Query**:
```sql
-- Fetch all data for comprehensive report
SELECT a.*, f.nom_ferme, f.lieu, c.* 
FROM analyse a
JOIN ferme f ON a.id_ferme = f.id_ferme
LEFT JOIN conseil c ON a.id_analyse = c.id_analyse
WHERE a.id_ferme = ? AND a.date_analyse BETWEEN ? AND ?
ORDER BY a.date_analyse DESC;
```

#### KISS Compliance
- Single report generation method
- Reuse existing PDF infrastructure
- Simple date range filtering
- Template-based generation

#### SRP Compliance
- `IntelligentReportService`: Only report generation
- `AnalyseService`: Only analysis CRUD (remove PDF logic)
- `ExpertDashboardController`: Only report triggering UI

#### FOSD Compliance
- Clear user story: US-EXPERT-V2
- Combines existing features meaningfully
- Provides value: comprehensive insights

---

## Feature Comparison Matrix

| Aspect | Visual Diagnosis | Intelligent Reports |
|--------|-----------------|---------------------|
| **Groq API** | Vision model (11b) | Text model (70b) |
| **New Service** | ExpertVisionService | IntelligentReportService |
| **New Model** | DiagnosisResult | ReportConfig |
| **UI Changes** | 1 button + dialog | 1 button + dialog |
| **Database Changes** | None | None |
| **Complexity** | Medium | Medium |
| **User Value** | High | High |
| **YAGNI** | ✓ Needed | ✓ Needed |
| **KISS** | ✓ Simple API call | ✓ Template-based |
| **SRP** | ✓ Separate service | ✓ Separate service |
| **FOSD** | ✓ Clear user story | ✓ Clear user story |

## Implementation Order

1. **Feature 1 First**: Visual Diagnosis
   - Establishes vision API infrastructure
   - Can be reused in Feature 2
   - Immediate user value

2. **Feature 2 Second**: Intelligent Reports
   - Builds on Feature 1 (includes visual results)
   - Consolidates existing scattered functionality
   - Provides comprehensive value

## Documentation References

1. **Groq Vision API**: https://console.groq.com/docs/vision
2. **PDFBox Best Practices**: https://pdfbox.apache.org/2.0/getting-started.html
3. **Java Base64 Encoding**: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Base64.html
4. **JavaFX FileChooser**: https://openjfx.io/javadoc/17/javafx.graphics/javafx/stage/FileChooser.html

## Status Update
- [x] Defined Feature 1: AI Visual Plant Disease Diagnosis
- [x] Defined Feature 2: Intelligent Analysis Report Generator
- [x] Validated against YAGNI/KISS/SRP/FOSD
- [x] Designed service and model classes
- [x] Specified API integration details
- [x] Cross-referenced official documentation
