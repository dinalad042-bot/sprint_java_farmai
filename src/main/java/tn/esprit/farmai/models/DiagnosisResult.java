package tn.esprit.farmai.models;

/**
 * Model class for AI visual plant disease diagnosis results.
 * Contains structured information about the diagnosis from Groq vision API.
 */
public class DiagnosisResult {
    
    public enum ConfidenceLevel {
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low");
        
        private final String label;
        
        ConfidenceLevel(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    private String condition;
    private ConfidenceLevel confidence;
    private String symptoms;
    private String treatment;
    private String prevention;
    private String urgency;
    private boolean needsExpertConsult;
    private String rawResponse;
    
    // Default constructor
    public DiagnosisResult() {}
    
    // Full constructor
    public DiagnosisResult(String condition, ConfidenceLevel confidence, String symptoms,
                          String treatment, String prevention, String urgency, 
                          boolean needsExpertConsult) {
        this.condition = condition;
        this.confidence = confidence;
        this.symptoms = symptoms;
        this.treatment = treatment;
        this.prevention = prevention;
        this.urgency = urgency;
        this.needsExpertConsult = needsExpertConsult;
    }
    
    // Getters and Setters
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public ConfidenceLevel getConfidence() {
        return confidence;
    }
    
    public void setConfidence(ConfidenceLevel confidence) {
        this.confidence = confidence;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public String getTreatment() {
        return treatment;
    }
    
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
    
    public String getPrevention() {
        return prevention;
    }
    
    public void setPrevention(String prevention) {
        this.prevention = prevention;
    }
    
    public String getUrgency() {
        return urgency;
    }
    
    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
    
    public boolean isNeedsExpertConsult() {
        return needsExpertConsult;
    }
    
    public void setNeedsExpertConsult(boolean needsExpertConsult) {
        this.needsExpertConsult = needsExpertConsult;
    }
    
    public String getRawResponse() {
        return rawResponse;
    }
    
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
    
    /**
     * Format the diagnosis as a structured text suitable for saving to analysis.
     */
    public String toAnalysisText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DIAGNOSTIC VISUEL IA ===\n\n");
        sb.append("Condition: ").append(condition).append("\n");
        sb.append("Confiance: ").append(confidence != null ? confidence.getLabel() : "N/A").append("\n");
        sb.append("Urgence: ").append(urgency).append("\n");
        sb.append("Consultation expert requise: ").append(needsExpertConsult ? "Oui" : "Non").append("\n\n");
        sb.append("Symptomes:\n").append(symptoms).append("\n\n");
        sb.append("Traitement:\n").append(treatment).append("\n\n");
        sb.append("Prevention:\n").append(prevention);
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "DiagnosisResult{" +
                "condition='" + condition + '\'' +
                ", confidence=" + confidence +
                ", urgency='" + urgency + '\'' +
                ", needsExpertConsult=" + needsExpertConsult +
                '}';
    }
}
