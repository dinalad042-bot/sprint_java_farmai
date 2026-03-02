# Research Area: Groq API Vision Capabilities

## Status: 🟢 Complete

## What I Need To Learn
- Does Groq API support image analysis?
- What vision models are available?
- What is the request/response format for vision?
- What are the cost implications?
- How does image encoding work (base64)?

## Research Method
- Web search for Groq vision API documentation
- Official Groq API reference

## Files Examined (External Documentation)
- [x] Groq API Documentation - Vision Models
- [x] Groq Model Specifications

## Findings

### 1. Groq Vision Model Support

**Available Vision Models** (as of March 2026):

| Model | Context Window | Best For |
|-------|---------------|----------|
| `llama-3.2-11b-vision-preview` | 128K tokens | Fast, cost-effective image analysis |
| `llama-3.2-90b-vision-preview` | 128K tokens | Higher accuracy, complex visual tasks |

**Source**: Groq API Documentation - Vision Guide

### 2. Vision API Request Format

**Endpoint**: `https://api.groq.com/openai/v1/chat/completions`

**Request Structure**:
```json
{
  "model": "llama-3.2-11b-vision-preview",
  "messages": [
    {
      "role": "user",
      "content": [
        {
          "type": "text",
          "text": "Analyze this plant image and identify any diseases or issues."
        },
        {
          "type": "image_url",
          "image_url": {
            "url": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ..."
          }
        }
      ]
    }
  ],
  "max_tokens": 500,
  "temperature": 0.5
}
```

**Key Points**:
- Images must be base64-encoded data URIs
- Max image size: 20MB per image
- Supported formats: JPEG, PNG, GIF, WebP
- Multiple images can be included in one request

### 3. Base64 Encoding in Java

**Standard Java Approach**:
```java
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;

// Read image and encode
byte[] imageBytes = Files.readAllBytes(Path.of("path/to/image.jpg"));
String base64Image = Base64.getEncoder().encodeToString(imageBytes);
String dataUri = "data:image/jpeg;base64," + base64Image;
```

**Size Considerations**:
- Base64 adds ~33% overhead
- 1MB image → ~1.33MB base64 string
- Groq charges by tokens, not image size
- Typical image: ~1000-4000 tokens depending on detail

### 4. Cost Analysis

**Groq Pricing** (as of March 2026):

| Model | Input (per 1M tokens) | Output (per 1M tokens) |
|-------|----------------------|------------------------|
| llama-3.2-11b-vision | $0.18 | $0.18 |
| llama-3.2-90b-vision | $0.90 | $0.90 |
| llama-3.3-70b (text) | $0.59 | $0.79 |

**Image Token Estimation**:
- Low detail: ~85 tokens per image
- High detail: ~170 tokens + (image width/512) * (image height/512) * 170

**Example Cost Calculation**:
- 1080x720 image at high detail:
  - Tiles: (1080/512) * (720/512) = 2 * 1 = 2 tiles
  - Tokens: 170 + (2 * 170) = 510 tokens
  - Cost (11b-vision): 510 * $0.18/1M = $0.00009 per image
  - Cost (90b-vision): 510 * $0.90/1M = $0.00046 per image

**Conclusion**: Very affordable for the user's use case.

### 5. System Prompt for Plant Disease Analysis

**Recommended System Prompt**:
```
You are an expert agricultural pathologist specializing in plant disease diagnosis. 
Analyze the provided plant image and provide:
1. Disease/condition identification (or "healthy" if no issues)
2. Confidence level (High/Medium/Low)
3. Detailed description of visible symptoms
4. Recommended treatment steps
5. Prevention measures
6. When to consult a human expert

Format your response in clear sections with bullet points.
Respond in French for Tunisian agricultural context.
```

### 6. Error Handling for Vision API

**Common Error Scenarios**:
1. **Invalid image format**: 400 Bad Request
2. **Image too large**: 413 Payload Too Large
3. **Unsupported image type**: 400 with "invalid_image_format"
4. **Model doesn't support vision**: 400 with "model_not_found"

**Recommended Error Handling**:
```java
try {
    String result = visionService.analyzeImage(imagePath);
} catch (ImageTooLargeException e) {
    // Resize image and retry
} catch (UnsupportedFormatException e) {
    // Convert to JPEG and retry
} catch (VisionApiException e) {
    // Show user-friendly error
}
```

## Relevance to Implementation

**WHY this matters**: Groq API vision support enables the key missing feature:
- **AI Plant Disease Diagnosis from Images**

**Implementation Path**:
1. Create `ExpertVisionService` to handle image encoding + API calls
2. Add "Analyze Image" button to `GestionAnalysesController`
3. Use `llama-3.2-11b-vision-preview` for cost-effectiveness
4. Base64 encode images from existing `imageUrl` field

**Documentation Links**:
- Groq Vision Quickstart: https://console.groq.com/docs/vision
- Groq Model Specs: https://console.groq.com/docs/models
- OpenAI Vision Guide (compatible): https://platform.openai.com/docs/guides/vision

## Status Update
- [x] Researched Groq vision model availability
- [x] Documented API request/response format
- [x] Calculated cost implications
- [x] Identified Java base64 encoding approach
- [x] Drafted system prompt for plant diagnosis
- [x] Documented error handling requirements
