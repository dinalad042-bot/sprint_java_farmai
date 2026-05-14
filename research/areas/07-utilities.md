# Research Area: Utility Classes Integration

## Status: 🟢 Complete

## What I Need To Learn
- Which utility classes conflict?
- Are implementations identical?
- What unique utilities exist per branch?

## Files Examined
- [x] `feature/expertise-is-alaeddin:utils/*` — Multiple utilities
- [x] `feature/securite-aymen:utils/*` — Multiple utilities
- [x] `feature/ferme-amen:utils/*` — Limited utilities

## Findings

### Utility Classes Conflict Matrix

| Utility Class | Expertise | Security | Ferme | Status |
|---------------|-----------|----------|-------|--------|
| `SessionManager.java` | ✓ | ✓ | - | **IDENTICAL** |
| `PasswordUtil.java` | ✓ | ✓ | - | **IDENTICAL** |
| `ProfileManager.java` | ✓ | ✓ | - | **IDENTICAL** |
| `NavigationUtil.java` | ✓ | ✓ | - | **DIFFERENT** |
| `NotificationManager.java` | ✓ | ✓ | - | **DIFFERENT** |

### Identical Utilities (No Merge Needed)

#### SessionManager.java
Both branches have **IDENTICAL** implementation:
```java
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    
    public static synchronized SessionManager getInstance() { ... }
    public void setCurrentUser(User user) { ... }
    public User getCurrentUser() { ... }
    public boolean isLoggedIn() { ... }
    public void logout() { ... }
    public boolean hasRole(String roleName) { ... }
    public boolean isAdmin() { ... }
    public boolean isExpert() { ... }
    public boolean isAgricole() { ... }
    public boolean isFournisseur() { ... }
}
```

#### PasswordUtil.java
Both branches have **IDENTICAL** implementation (BCrypt-like hashing).

#### ProfileManager.java
Both branches have **IDENTICAL** implementation for profile UI updates.

### Different Utilities (Need Merge)

#### NavigationUtil.java

**Expertise Version**:
- `navigateToDashboard(Stage)` — Routes based on user role
- `navigateToSignup(Stage)`
- `navigateTo(Stage, String, String)`
- `logout(Stage)`
- `showError(String, String)`
- `showSuccess(String, String)`
- `showWarning(String, String)`

**Security Version** (Same + Additional):
- All Expertise methods PLUS:
- Additional navigation methods for verification flow

**RESOLUTION**: Use Security version (has all methods)

#### NotificationManager.java

**Expertise Version**:
- Basic notification display
- Toast notifications

**Security Version**:
- Same as Expertise

**RESOLUTION**: Either version works

### Unique Utilities per Branch

#### Expertise Branch Unique
| Utility | Purpose |
|---------|---------|
| `WeatherUtils.java` | Weather API integration |
| `SpeechUtils.java` | Text-to-speech |
| `SimpleHttpClient.java` | HTTP client for Groq API |
| `Config.java` | Configuration management |
| `AnalyseDialog.java` | Analysis dialog helper |
| `AlertUtils.java` | Alert dialogs |

#### Security Branch Unique
| Utility | Purpose |
|---------|---------|
| `MailingService.java` | Email sending (SMTP) |

#### Ferme Branch Unique
| Utility | Purpose |
|---------|---------|
| (None - uses services directly) | |

### Merged Utility Classes List

```
src/main/java/tn/esprit/farmai/utils/
├── MyDBConnexion.java        (Base - from main)
├── SessionManager.java        (Either - identical)
├── PasswordUtil.java          (Either - identical)
├── ProfileManager.java        (Either - identical)
├── NavigationUtil.java        (Security - more complete)
├── NotificationManager.java   (Either - identical)
├── MailingService.java        (Security - email)
├── WeatherUtils.java          (Expertise - weather)
├── SpeechUtils.java           (Expertise - TTS)
├── SimpleHttpClient.java      (Expertise - HTTP)
├── Config.java                (Expertise - config)
├── AnalyseDialog.java         (Expertise - dialog)
└── AlertUtils.java            (Expertise - alerts)
```

### MailingService.java (Security Branch)
```java
package tn.esprit.farmai.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class MailingService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USER = "your-email@gmail.com";
    private static final String SMTP_PASSWORD = "your-app-password";
    
    public static void sendMail(String to, String subject, String body) {
        // SMTP configuration
        // Session creation
        // Message sending
    }
}
```

### WeatherUtils.java (Expertise Branch)
```java
package tn.esprit.farmai.utils;

public class WeatherUtils {
    // Weather API integration
    // Temperature, humidity, forecast
}
```

### SimpleHttpClient.java (Expertise Branch)
```java
package tn.esprit.farmai.utils;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;
import org.json.*;

public class SimpleHttpClient {
    public static JSONObject get(String url) { ... }
    public static JSONObject post(String url, JSONObject body) { ... }
}
```

## Relevance to Implementation
Utility class integration is straightforward:
1. Use identical utilities from either branch
2. Use Security NavigationUtil (more complete)
3. Include all unique utilities from each branch
4. No breaking changes expected

## Status Update
- [x] Analyzed all utility classes
- [x] Identified identical implementations
- [x] Identified different implementations
- [x] Listed unique utilities per branch
- [x] Created merged utility list
