package com.example.aiassistant.service;

import com.example.aiassistant.dto.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    
    private static final String GROQ_DEFAULT_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String OPENROUTER_DEFAULT_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String GEMINI_DIRECT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

    private final String groqApiKey;
    private final String groqModel;
    private final String openRouterApiKey;
    private final String openRouterModel;
    private final String geminiApiKey;
    private final String geminiModel;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiService(
            @Value("${groq.api.key:}") String groqApiKey,
            @Value("${groq.model:llama-3.3-70b-versatile}") String groqModel,
            @Value("${openrouter.api.key:}") String openRouterApiKey,
            @Value("${openrouter.model:google/gemini-2.5-flash}") String openRouterModel,
            @Value("${gemini.api.key:}") String geminiApiKey,
            @Value("${gemini.model:gemini-1.5-flash}") String geminiModel,
            ObjectMapper objectMapper) {
        this.groqApiKey = groqApiKey != null ? groqApiKey.trim() : "";
        this.groqModel = (groqModel != null && !groqModel.trim().isEmpty())
                ? groqModel.trim()
                : "llama-3.3-70b-versatile";
        this.openRouterApiKey = openRouterApiKey != null ? openRouterApiKey.trim() : "";
        this.openRouterModel = (openRouterModel != null && !openRouterModel.trim().isEmpty())
                ? openRouterModel.trim()
                : "google/gemini-2.5-flash";
        this.geminiApiKey = geminiApiKey != null ? geminiApiKey.trim() : "";
        this.geminiModel = (geminiModel != null && !geminiModel.trim().isEmpty())
                ? geminiModel.trim()
                : "gemini-1.5-flash";
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Sends conversation messages to Groq (or OpenRouter/Gemini) and returns the generated text.
     */
    public String generateResponse(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Messages cannot be null or empty.");
        }

        // Priority 1: Groq API Key (starts with gsk_ or GROQ_API_KEY is populated)
        if (isGroqConfigured()) {
            return generateViaGroq(messages);
        }
        // Priority 2: OpenRouter API Key (starts with sk-or- or OPENROUTER_API_KEY is populated)
        else if (isOpenRouterConfigured()) {
            return generateViaOpenRouter(messages);
        }
        // Priority 3: Google Gemini API Direct Key
        else if (isGeminiConfigured()) {
            return generateViaGeminiDirect(messages);
        } else {
            log.error("No API key configured for Groq, OpenRouter, or Gemini.");
            throw new IllegalStateException(
                    "API Key is not configured. Please set GROQ_API_KEY (from https://console.groq.com/keys) in backend/.env"
            );
        }
    }

    /**
     * Call Groq Cloud API (Super fast inference with Llama 3.3, Mixtral, Gemma, etc.)
     */
    private String generateViaGroq(List<Message> messages) {
        String activeKey = !groqApiKey.isEmpty() ? groqApiKey : 
                          (!openRouterApiKey.isEmpty() && openRouterApiKey.startsWith("gsk_") ? openRouterApiKey : geminiApiKey);

        log.info("Sending request to Groq API (model: {}, message count: {})", groqModel, messages.size());

        List<Map<String, String>> formattedMessages = new ArrayList<>();
        for (Message msg : messages) {
            if (msg.getContent() != null && !msg.getContent().trim().isEmpty()) {
                Map<String, String> item = new HashMap<>();
                item.put("role", "assistant".equalsIgnoreCase(msg.getRole()) ? "assistant" : "user");
                item.put("content", msg.getContent().trim());
                formattedMessages.add(item);
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", groqModel);
        payload.put("messages", formattedMessages);

        try {
            String responseJson = restClient.post()
                    .uri(GROQ_DEFAULT_URL)
                    .header("Authorization", "Bearer " + activeKey)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            return extractTextFromOpenAiFormat(responseJson, "Groq");

        } catch (HttpClientErrorException e) {
            log.error("Groq API error: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new RuntimeException("Groq authentication failed. Please verify your GROQ_API_KEY from https://console.groq.com/keys.");
            } else if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("Groq rate limit reached. Please wait a moment before sending another message.");
            }
            throw new RuntimeException("Groq API error (" + e.getStatusCode().value() + "): " + extractErrorMessage(e.getResponseBodyAsString()));
        } catch (HttpServerErrorException e) {
            log.error("Groq server error: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Groq service is temporarily unavailable. Please try again later.");
        } catch (ResourceAccessException e) {
            log.error("Network error communicating with Groq: {}", e.getMessage());
            throw new RuntimeException("Network error connecting to Groq API. Please check your internet connection.");
        } catch (Exception e) {
            log.error("Unexpected error during Groq call: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while generating response.");
        }
    }

    /**
     * Call OpenRouter API
     */
    private String generateViaOpenRouter(List<Message> messages) {
        String activeKey = !openRouterApiKey.isEmpty() ? openRouterApiKey : geminiApiKey;

        log.info("Sending request to OpenRouter API (model: {}, message count: {})", openRouterModel, messages.size());

        List<Map<String, String>> formattedMessages = new ArrayList<>();
        for (Message msg : messages) {
            if (msg.getContent() != null && !msg.getContent().trim().isEmpty()) {
                Map<String, String> item = new HashMap<>();
                item.put("role", "assistant".equalsIgnoreCase(msg.getRole()) ? "assistant" : "user");
                item.put("content", msg.getContent().trim());
                formattedMessages.add(item);
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", openRouterModel);
        payload.put("messages", formattedMessages);

        try {
            String responseJson = restClient.post()
                    .uri(OPENROUTER_DEFAULT_URL)
                    .header("Authorization", "Bearer " + activeKey)
                    .header("HTTP-Referer", "http://localhost:5173")
                    .header("X-Title", "AI Assistant")
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            return extractTextFromOpenAiFormat(responseJson, "OpenRouter");

        } catch (HttpClientErrorException e) {
            log.error("OpenRouter API error: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new RuntimeException("OpenRouter authentication failed. Please verify your OPENROUTER_API_KEY.");
            } else if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("OpenRouter rate limit exceeded. Please wait a moment.");
            }
            throw new RuntimeException("OpenRouter API error: " + extractErrorMessage(e.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("Unexpected error during OpenRouter call: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.");
        }
    }

    /**
     * Call Google Gemini API directly
     */
    private String generateViaGeminiDirect(List<Message> messages) {
        log.info("Sending request to Google Gemini API (model: {}, message count: {})", geminiModel, messages.size());

        Map<String, Object> requestPayload = buildGeminiPayload(messages);
        String endpointUrl = String.format("%s/%s:generateContent", GEMINI_DIRECT_BASE_URL, geminiModel);

        try {
            String responseJson = restClient.post()
                    .uri(endpointUrl)
                    .header("x-goog-api-key", geminiApiKey)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);

            return extractTextFromGeminiResponse(responseJson);

        } catch (HttpClientErrorException e) {
            log.error("Gemini API error: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new RuntimeException("Gemini API authentication failed. Please verify your GEMINI_API_KEY.");
            }
            throw new RuntimeException("Gemini API error: " + extractErrorMessage(e.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("Unexpected error during Gemini API invocation: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.");
        }
    }

    private boolean isGroqConfigured() {
        if (!groqApiKey.isEmpty() && !groqApiKey.startsWith("your_")) return true;
        if (!openRouterApiKey.isEmpty() && openRouterApiKey.startsWith("gsk_")) return true;
        return !geminiApiKey.isEmpty() && geminiApiKey.startsWith("gsk_");
    }

    private boolean isOpenRouterConfigured() {
        if (!openRouterApiKey.isEmpty() && !openRouterApiKey.startsWith("your_")) return true;
        return !geminiApiKey.isEmpty() && geminiApiKey.startsWith("sk-or-");
    }

    private boolean isGeminiConfigured() {
        return !geminiApiKey.isEmpty() && !geminiApiKey.startsWith("your_");
    }

    private Map<String, Object> buildGeminiPayload(List<Message> messages) {
        List<Map<String, Object>> contents = new ArrayList<>();
        for (Message msg : messages) {
            if (msg.getContent() == null || msg.getContent().trim().isEmpty()) continue;
            String geminiRole = "assistant".equalsIgnoreCase(msg.getRole()) || "model".equalsIgnoreCase(msg.getRole())
                    ? "model"
                    : "user";

            Map<String, Object> contentItem = new HashMap<>();
            contentItem.put("role", geminiRole);

            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> textPart = new HashMap<>();
            textPart.put("text", msg.getContent().trim());
            parts.add(textPart);

            contentItem.put("parts", parts);
            contents.add(contentItem);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", contents);
        return payload;
    }

    private String extractTextFromOpenAiFormat(String responseJson, String providerName) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode choices = rootNode.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode contentNode = choices.get(0).path("message").path("content");
                if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                    return contentNode.asText();
                }
            }
            throw new RuntimeException("No response choices returned by " + providerName + ".");
        } catch (Exception e) {
            log.error("Failed to parse {} response: {}", providerName, e.getMessage());
            throw new RuntimeException("Failed to parse response: " + e.getMessage());
        }
    }

    private String extractTextFromGeminiResponse(String responseJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode candidatesNode = rootNode.path("candidates");

            if (candidatesNode.isArray() && !candidatesNode.isEmpty()) {
                JsonNode partsNode = candidatesNode.get(0).path("content").path("parts");
                if (partsNode.isArray() && !partsNode.isEmpty()) {
                    StringBuilder fullText = new StringBuilder();
                    for (JsonNode part : partsNode) {
                        JsonNode textNode = part.path("text");
                        if (!textNode.isMissingNode()) {
                            fullText.append(textNode.asText());
                        }
                    }
                    if (fullText.length() > 0) return fullText.toString();
                }
            }
            throw new RuntimeException("No text candidate returned from Gemini API.");
        } catch (Exception e) {
            log.error("Failed to parse Gemini API response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse response from Gemini: " + e.getMessage());
        }
    }

    private String extractErrorMessage(String errorBody) {
        if (errorBody == null || errorBody.isEmpty()) return "Unknown error";
        try {
            JsonNode root = objectMapper.readTree(errorBody);
            JsonNode msg = root.path("error").path("message");
            if (!msg.isMissingNode() && !msg.asText().isEmpty()) return msg.asText();
        } catch (Exception ignored) {}
        return "Service request failed";
    }
}
