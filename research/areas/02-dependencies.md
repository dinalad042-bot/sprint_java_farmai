# Research Area: Dependency Analysis (pom.xml)

## Status: 🟢 Complete

## What I Need To Learn
- What dependencies does each branch add?
- Are there version conflicts?
- What needs to be merged?

## Files Examined
- [x] `main:pom.xml` — Base dependencies
- [x] `feature/expertise-is-alaeddin:pom.xml` — Expertise additions
- [x] `feature/securite-aymen:pom.xml` — Security additions
- [x] `feature/ferme-amen:pom.xml` — Ferme additions

## Findings

### Base Dependencies (main branch)
| GroupId | ArtifactId | Version | Purpose |
|---------|------------|---------|---------|
| org.openjfx | javafx-controls | 17.0.6 | JavaFX UI |
| org.openjfx | javafx-fxml | 17.0.6 | JavaFX FXML |
| org.openjfx | javafx-web | 17.0.6 | JavaFX WebView |
| org.openjfx | javafx-media | 17.0.6 | JavaFX Media |
| org.controlsfx | controlsfx | 11.1.2 | Extended controls |
| com.dlsc | formsfx-core | 11.6.0 | Forms framework |
| net.synedra | validatorfx | 0.4.0 | Validation |
| org.kordamp | ikonli-javafx | 12.3.1 | Icons |
| org.kordamp.bootstrapfx | bootstrapfx-core | 0.4.0 | Bootstrap styling |
| eu.hansolo | tilesfx | 17.1.17 | Dashboard tiles |
| com.github.almasb | fxgl | 17.2 | Game framework |
| org.junit.jupiter | junit-jupiter-api | 5.9.2 | Testing |
| org.junit.jupiter | junit-jupiter-engine | 5.9.2 | Testing |
| com.mysql | mysql-connector-j | 8.2.0 | MySQL database |

### Branch 1: feature/expertise-is-alaeddin Additions
| GroupId | ArtifactId | Version | Purpose |
|---------|------------|---------|---------|
| org.apache.pdfbox | pdfbox | 2.0.29 | PDF generation |
| com.itextpdf | kernel | 7.2.5 | iText PDF kernel |
| com.itextpdf | layout | 7.2.5 | iText PDF layout |
| org.apache.httpcomponents | httpclient | 4.5.14 | HTTP client for Groq API |
| org.json | json | 20231013 | JSON processing |
| org.jfree | jfreechart | 1.5.3 | Charts for statistics |

**Uses**: PDF reports, Groq AI API calls, Statistics charts

### Branch 2: feature/securite-aymen Additions
| GroupId | ArtifactId | Version | Purpose |
|---------|------------|---------|---------|
| org.bytedeco | javacv-platform | 1.5.10 | OpenCV face recognition |
| com.sun.mail | jakarta.mail | 2.0.1 | Email service |
| com.sun.activation | jakarta.activation | 2.0.1 | Email activation |
| org.junit.platform | junit-platform-launcher | 1.9.2 | Test launcher |

**Uses**: Face recognition, Email OTP, Face login

### Branch 3: feature/ferme-amen Additions
| GroupId | ArtifactId | Version | Purpose |
|---------|------------|---------|---------|
| com.itextpdf | kernel | 7.2.5 | iText PDF kernel |
| com.itextpdf | io | 7.2.5 | iText IO |
| com.itextpdf | layout | 7.2.5 | iText PDF layout |
| com.itextpdf | font-asian | 7.2.5 | iText Asian fonts |
| net.sf.freetts | freetts | 1.2.2 | Text-to-speech |
| com.google.cloud | google-cloud-speech | 2.26.0 | Speech recognition |
| org.json | json | 20231013 | JSON processing |

**Uses**: PDF generation, Voice features, Speech-to-text

## Version Conflicts Analysis

### No Conflicts (Same Versions)
| Artifact | Version | Branches |
|----------|---------|----------|
| javafx-* | 17.0.6 | All |
| mysql-connector-j | 8.2.0 | All |
| json | 20231013 | expertise + ferme |
| itext (kernel, layout) | 7.2.5 | expertise + ferme |

### Potential Conflicts (Different Versions)
None detected - all overlapping dependencies use same versions.

## Merged pom.xml Strategy

### Required Dependencies (All branches combined)
```xml
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
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-web</artifactId>
    <version>17.0.6</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-media</artifactId>
    <version>17.0.6</version>
</dependency>

<!-- Database -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
</dependency>

<!-- PDF Generation (iText) -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>kernel</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>io</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>layout</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>font-asian</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.29</version>
</dependency>

<!-- Face Recognition (Security) -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.10</version>
</dependency>

<!-- Email (Security) -->
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>jakarta.mail</artifactId>
    <version>2.0.1</version>
</dependency>
<dependency>
    <groupId>com.sun.activation</groupId>
    <artifactId>jakarta.activation</artifactId>
    <version>2.0.1</version>
</dependency>

<!-- HTTP Client (Expertise) -->
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.14</version>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20231013</version>
</dependency>

<!-- Charts (Expertise) -->
<dependency>
    <groupId>org.jfree</groupId>
    <artifactId>jfreechart</artifactId>
    <version>1.5.3</version>
</dependency>

<!-- Voice/Speech (Ferme) -->
<dependency>
    <groupId>net.sf.freetts</groupId>
    <artifactId>freetts</artifactId>
    <version>1.2.2</version>
</dependency>
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-speech</artifactId>
    <version>2.26.0</version>
</dependency>

<!-- UI Components -->
<dependency>
    <groupId>org.controlsfx</groupId>
    <artifactId>controlsfx</artifactId>
    <version>11.1.2</version>
</dependency>
<dependency>
    <groupId>com.dlsc.formsfx</groupId>
    <artifactId>formsfx-core</artifactId>
    <version>11.6.0</version>
</dependency>
<dependency>
    <groupId>org.kordamp.ikonli</groupId>
    <artifactId>ikonli-javafx</artifactId>
    <version>12.3.1</version>
</dependency>
<dependency>
    <groupId>org.kordamp.bootstrapfx</groupId>
    <artifactId>bootstrapfx-core</artifactId>
    <version>0.4.0</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-launcher</artifactId>
    <version>1.9.2</version>
</dependency>
```

## Relevance to Implementation
The merged pom.xml must include all dependencies from all three branches. No version conflicts exist, so merge is straightforward. The google-cloud-speech dependency is large and may require additional configuration.

## Status Update
- [x] Analyzed all pom.xml files
- [x] Identified unique dependencies per branch
- [x] Checked for version conflicts
- [x] Created merged dependency list
