# Research Area: Captcha Feature (from securite-aymen)

## Status: 🔴 Not Started

## What I Need To Learn
1. Where is the captcha implemented in the securite-aymen branch?
2. What type of captcha is used? (image-based, Google reCAPTCHA, simple math?)
3. What files were modified/created for captcha?
4. What dependencies are required?
5. How does it integrate with login/signup flow?

## Files To Examine (from securite-aymen branch)
- [ ] LoginController.java - likely location
- [ ] SignupController.java - possible location
- [ ] VerificationController.java - possible location
- [ ] Any new captcha-related service/utils
- [ ] FXML files for UI changes
- [ ] pom.xml for dependencies

## Investigation Plan

```bash
# Show diff between main and securite-aymen
git diff main..origin/feature/securite-aymen --name-only

# Show detailed changes
git diff main..origin/feature/securite-aymen
```

## Findings

### Captcha Implementation
*To be filled after investigation*

### Files Modified
*To be filled after investigation*

### Dependencies Required
*To be filled after investigation*

## Relevance to Implementation
The captcha feature needs to be pulled from securite-aymen and integrated into integration-final without breaking existing authentication flow.

## Status Update
- [ ] Fetch securite-aymen branch details
- [ ] Identify captcha-related files
- [ ] Document implementation details
- [ ] Note dependencies
