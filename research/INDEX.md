# Research Index - Expert Module Enhancement

## Goal
Add 2 advanced functionalities to the Expert module using Groq API (with image analysis capabilities) following YAGNI, KISS, SRP, and FOSD principles. Cross-reference with official documentation.

## Status: 🟢 COMPLETE

## Research Areas

| # | Area | Status | Doc | Key Finding |
|---|------|--------|-----|-------------|
| 1 | Expert Module Analysis | 🟢 Complete | [14-expert-module-analysis.md](areas/14-expert-module-analysis.md) | Current features: chatbot, voice, analyses, conseils, PDF export |
| 2 | Agricole Features Gap | 🟢 Complete | [15-agricole-features-gap.md](areas/15-agricole-features-gap.md) | Agri has: meteo, irrigation AI, market export, plant CRUD |
| 3 | Groq API Capabilities | 🟢 Complete | [16-groq-api-vision.md](areas/16-groq-api-vision.md) | Groq supports vision models (llama-3.2-11b/90b-vision) for image analysis |
| 4 | YAGNI/KISS/SRP Audit | 🟢 Complete | [17-principles-audit.md](areas/17-principles-audit.md) | 8 violations identified with refactoring plans |
| 5 | Feature Design | 🟢 Complete | [18-feature-design.md](areas/18-feature-design.md) | 2 features designed: Visual Diagnosis + Intelligent Reports |
| 6 | Implementation Plan | 🟢 Complete | [PLAN.md](PLAN.md) | 3-phase implementation with code samples |

## Current Position
📍 Research complete — Implementation plan ready for review

## Discovery Log (Chronological)
- [2026-03-02] Phase 4 Started - Expert module enhancement research
- [2026-03-02] Analyzed ExpertDashboardController - has statistics, face login, analyses/conseils nav
- [2026-03-02] Analyzed ExpertChatbotService - uses Groq llama-3.3-70b, text-only currently
- [2026-03-02] Analyzed GestionAnalysesController - has AI diagnostic (text-based), PDF export
- [2026-03-02] Analyzed Agricole features - PlantesController has weather-based irrigation AI
- [2026-03-02] **FOUND** Groq API supports vision models (llama-3.2-11b-vision-preview, llama-3.2-90b-vision-preview)
- [2026-03-02] Identified Gap: Expert module has no image analysis capability for plant disease diagnosis
- [2026-03-02] Identified Gap: Expert module has no predictive/recommendation engine combining multiple data sources

## Open Questions Count: 2
See [questions.md](questions.md)

## Blockers
- [ ] Need to verify Groq API credits available for vision models
- [ ] Need to determine if llama-3.2-11b-vision or 90b-vision is preferred (cost vs accuracy tradeoff)

## Verified Already Working
- ✅ ExpertChatbotService works with Groq API
- ✅ AnalyseService.generateAIDiagnostic() uses Groq for text analysis
- ✅ PDF export for analyses working (Apache PDFBox)
- ✅ Image URL storage in analyses working
- ✅ WeatherService integration available for enrichment

## Analysis Summary
| Feature | Agricole | Expert | Gap |
|---------|----------|--------|-----|
| Weather Data | ✅ | ❌ | Missing |
| Irrigation AI | ✅ | ❌ | Missing |
| Plant Image Analysis | ❌ | ❌ | Opportunity |
| Disease Diagnosis (Image) | ❌ | ❌ | Opportunity |
| Market/Export Calc | ✅ | ❌ | Missing |
| Voice TTS | ❌ | ✅ | Expert has this |
| Chatbot AI | ❌ | ✅ | Expert has this |
| PDF Reports | ✅ | ✅ | Both have |
