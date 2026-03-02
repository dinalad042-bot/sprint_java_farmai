# Open Questions

## Answerable by Code (Need More Research)

### Q14: What image sizes should we support for vision API?
- [ ] **PENDING** — Need to determine optimal image dimensions
- **Question**: Should we resize images before sending to Groq API?
- **Considerations**: Cost (tokens), quality, API limits
- **Likely approach**: Resize to max 1024x1024 to balance quality/cost
- **Assigned to**: Feature 1 implementation

### Q15: Should we cache vision API results?
- [ ] **PENDING** — Need to decide on caching strategy
- **Question**: Same image analyzed twice = same API cost or cached?
- **Options**: 
  - No cache (simplest, KISS)
  - Hash-based cache (more complex)
- **Assigned to**: Feature 1 implementation

## Need Human Input

### Q16: Groq API Vision Model Selection
- [ ] **PENDING** — Need user preference
- **Question**: Which vision model should we use?
- **Options**: 
  - `llama-3.2-11b-vision-preview` - Faster, cheaper ($0.18/1M tokens)
  - `llama-3.2-90b-vision-preview` - Higher accuracy ($0.90/1M tokens)
- **Recommendation**: Start with 11b for cost-effectiveness
- **Assigned to**: Feature 1 implementation

### Q17: Report Format Preferences
- [ ] **PENDING** — Need user confirmation
- **Question**: What sections should the intelligent report include?
- **Proposed sections**:
  1. Executive Summary (AI-generated)
  2. Weather Context
  3. Analysis History
  4. Diagnosis Summary
  5. Recommendations
  6. Action Plan
- **Assigned to**: Feature 2 implementation

## Resolved

- [x] **Q1-Q9**: Previous branch integration questions — RESOLVED in Phase 1-3
- [x] **Q10-Q12**: Agricole integration issues — RESOLVED with PasswordFixer
- [x] **Q13**: Debug logging decision — Not needed, issue resolved
- [x] **Q18**: Can Groq API analyze images? — YES, llama-3.2-vision models support this
- [x] **Q19**: Does codebase have image infrastructure? — YES, Analyse.imageUrl field exists
- [x] **Q20**: Are there YAGNI violations to remove first? — YES, HTML export in AnalyseService

## Summary
- **Total Questions**: 17
- **Resolved by Research**: 20
- **Pending Human Input**: 2 (Q16, Q17)
- **Pending Code Research**: 2 (Q14, Q15)

## Notes
All critical blockers resolved. Implementation can proceed with documented assumptions.
