# Research Area: Expert Module Current State Analysis

## Status: 🟢 Complete

## What I Need To Learn
- What features currently exist in the expert module?
- How is Groq API currently being used?
- What data models does the expert module work with?
- What UI controllers manage expert functionality?

## Files Examined
- [x] `ExpertDashboardController.java:1-244` — Main expert dashboard with statistics
- [x] `ExpertChatbotService.java:1-73` — Groq API integration for text chatbot
- [x] `ExpertVoiceService.java:1-47` — Text-to-speech using Windows Speech API
- [x] `GestionAnalysesController.java:1-683` — Analysis management with AI diagnostic
- [x] `GestionConseilsController.java:1-470` — Recommendations management with TTS
- [x] `AnalyseService.java:1-573` — CRUD + AI diagnostic + PDF export + weather enrichment
- [x] `Analyse.java:1-97` — Analysis model with imageUrl field

## Findings

### Current Expert Features

#### 1. Expert Dashboard (`ExpertDashboardController.java:1-244`)
**Location**: `src/main/java/tn/esprit/farmai/controllers/ExpertDashboardController.java`

**Features**:
- Statistics display (total analyses, conseils, fermes) - lines 84-109
- Navigation to analyses management - line 127
- Navigation to conseils management - line 135
- Statistics view - line 143
- Face enrollment for biometric login - lines 149-177

**Code Pattern**: Uses fade transitions for navigation (lines 190-244)

#### 2. Expert Chatbot (`ExpertChatbotService.java:1-73`)
**Location**: `src/main/java/tn/esprit/farmai/services/ExpertChatbotService.java`

**Features**:
- Groq API integration with llama-3.3-70b-versatile model - line 17
- System prompt for Tunisian agricultural/veterinary expert - lines 37-44
- Temperature 0.5 for stable responses - line 48
- Max 500 tokens to limit cost - line 49

**Current Limitation**: Text-only, no image support

#### 3. Expert Voice (`ExpertVoiceService.java:1-47`)
**Location**: `src/main/java/tn/esprit/farmai/services/ExpertVoiceService.java`

**Features**:
- Windows PowerShell TTS - lines 18-24
- macOS `say` command - line 26
- Process management for stopping speech - lines 35-46

#### 4. Analysis Management (`GestionAnalysesController.java:1-683`)
**Location**: `src/main/java/tn/esprit/farmai/controllers/GestionAnalysesController.java`

**Features**:
- Full CRUD for Analyse entities
- AI-assisted diagnostic (text-based) - lines 78-195
- PDF export with Apache PDFBox - lines 197-297
- Image display in table - lines 406-438
- Search and filter functionality

**AI Diagnostic Flow** (lines 78-195):
1. User selects analysis
2. Dialog prompts for observations
3. Calls `analyseService.generateAIDiagnostic()` 
4. Displays AI result with option to edit
5. Saves to database

#### 5. Analysis Service (`AnalyseService.java:1-573`)
**Location**: `src/main/java/tn/esprit/farmai/services/AnalyseService.java`

**Key Methods**:
- `generateAIDiagnostic()` - lines 68-152: Groq API for text analysis
- `exportAnalysisToPDF()` - lines 236-334: PDF generation with images
- `enrichWithWeather()` - lines 516-571: Weather data integration
- `getConseilsByAnalyse()` - lines 488-505: 1:N relationship handling

**AI Diagnostic Implementation** (lines 68-152):
```java
// Uses Config.GROQ_API_KEY for authentication
// Builds JSON request with system and user messages
// Handles various error conditions (401, 429, quota, etc.)
```

#### 6. Analysis Model (`Analyse.java:1-97`)
**Location**: `src/main/java/tn/esprit/farmai/models/Analyse.java`

**Fields**:
- `idAnalyse`: Primary key
- `dateAnalyse`: Timestamp
- `resultatTechnique`: Diagnostic text
- `idTechnicien`: FK to User
- `idFerme`: FK to Ferme
- `imageUrl`: String path to image (NOT BLOB)

**Note**: Image storage uses URL/path, not database BLOB (good practice)

### Code Patterns Observed

1. **Groq API Pattern**: All AI calls use `SimpleHttpClient.postJson()` with Bearer auth
2. **Error Handling**: Detailed error messages for different API failure modes
3. **Async Operations**: AI calls run in background threads with Platform.runLater() for UI updates
4. **Image Handling**: URLs stored in database, files stored on filesystem
5. **PDF Generation**: Apache PDFBox with proper resource management (try-with-resources)

## Relevance to Implementation

**WHY this matters**: The expert module already has a solid foundation with:
- Groq API integration infrastructure
- Image URL storage capability
- PDF export functionality
- Weather enrichment (though not exposed in UI)

**Gap Identified**: No image analysis capability - the AI diagnostic is text-only even though images can be attached to analyses.

**Opportunity**: Add vision-based AI analysis to leverage existing image infrastructure.

## New Questions Generated
- Can we upgrade ExpertChatbotService to support image inputs for plant disease questions?
- Should we add a new "Visual Diagnostic" feature separate from text-based analysis?
- How should we handle image preprocessing before sending to Groq vision API?

## Status Update
- [x] Read all expert-related source files
- [x] Document current feature set
- [x] Identify infrastructure capabilities
- [x] Identify feature gaps
- [x] Note code patterns for consistency
