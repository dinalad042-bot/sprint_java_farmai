# Research Area: CSS Architecture

## Status: 🟢 Complete

## What I Need To Learn
- Current CSS file structure and organization
- Color palette inconsistencies between auth and dashboard
- Button class naming inconsistencies
- Table visibility issues mentioned in requirements

## Files Examined
- [x] `src/main/resources/tn/esprit/farmai/styles/auth.css` — Authentication styles (login/signup)
- [x] `src/main/resources/tn/esprit/farmai/styles/dashboard.css` — Dashboard and app styles

## Findings

### Color Palette Inconsistencies

**auth.css Primary Green:**
- Primary: `#2E7D32` (line 10 - gradient start)
- Secondary: `#1B5E20` (line 10 - gradient end)
- Button: `#4CAF50` (line 134 - .primary-button)
- Button Hover: `#66BB6A` (line 140)
- Focus Border: `#4CAF50` (line 97)

**dashboard.css Primary Green:**
- Sidebar Profile Card: `#388E3C` (line 27)
- Nav Button Active: `#2E7D32` (line 368)
- Secondary Button Border: `#2E7D32` (line 279)

**Conflict:** auth.css uses `#4CAF50` for primary buttons while dashboard.css sidebar uses `#388E3C`

### Button Class Name Inconsistencies

**auth.css:**
```css
.primary-button { ... }  /* line 134 */
```

**dashboard.css:**
```css
.primary-btn { ... }      /* line 256 */
.secondary-btn { ... }    /* line 268 */
.danger-btn { ... }       /* line 280 */
.action-btn { ... }       /* line 290 */
.info-btn { ... }         /* line 302 */
.warning-btn { ... }      /* line 316 */
```

**Issue:** `.primary-button` (auth) vs `.primary-btn` (dashboard) — different naming conventions

### Input Field Inconsistencies

**auth.css (lines 86-109):**
```css
.auth-input {
    -fx-background-radius: 8px;
    -fx-border-radius: 8px;
    -fx-padding: 14px 16px;
    -fx-pref-height: 50px;
}
```

**dashboard.css (lines 444-459):**
```css
.dialog-pane .text-field {
    -fx-background-radius: 10px;
    -fx-padding: 12px;
}
```

**Issue:** Border radius differs (8px vs 10px), padding differs (14px/16px vs 12px)

### Table Visibility Fixes Already Applied

**dashboard.css (lines 119-165):**
Already has comprehensive table fixes:
```css
.table-row-cell {
    -fx-min-height: 45px;
    -fx-pref-height: 55px;
    -fx-cell-size: 55px;
}

.table-cell {
    -fx-text-fill: #000000;
    -fx-padding: 10px 12px;
}

/* Force visible text in all table cells */
.table-row-cell .table-cell {
    -fx-text-fill: black !important;
}
```

### Background Colors

**auth.css:**
- Main background: `#f5f5f5` (line 8)
- Left panel: `linear-gradient(to bottom, #2E7D32, #1B5E20)` (line 13)
- Right panel: `#424242` (line 78)
- Input background: `#263238` (line 90)

**dashboard.css:**
- Dashboard root: `#F4F6F8` (line 7)
- Sidebar: `#263238` (line 12)
- Profile card: `#388E3C` (line 27)

## Code Patterns Observed
- Both files use `-fx-effect: dropshadow(three-pass-box, ...)` consistently
- Both use "Segoe UI" as primary font family
- Both use similar border-radius patterns (8-10px)

## Relevance to Implementation
**CRITICAL:** To unify the UI, we must:
1. Create a `root.css` with CSS variables for all colors
2. Standardize on one green primary color (`#2E7D32` recommended - used in both)
3. Consolidate button classes to use `-btn` suffix consistently
4. Merge auth.css and dashboard.css into main.css
5. Ensure input fields have consistent 8px border-radius and 12px padding

## New Questions Generated
- Should dark theme (#1a1a2e from face-recognition) be integrated as a theme variant? → Added to questions.md

## Status Update
- [x] Examined auth.css completely
- [x] Examined dashboard.css completely
- [x] Documented all color inconsistencies
- [x] Documented class naming issues
- [x] Verified table visibility fixes already present
