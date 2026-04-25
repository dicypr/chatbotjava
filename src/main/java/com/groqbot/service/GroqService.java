package com.groqbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.groqbot.model.ChatMessage;
import com.groqbot.model.ChatRequest;
import com.groqbot.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class GroqService {

    // ── Config ──────────────────────────────────────────────────────────────
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama3-70b-versatile";
    private static final String SYSTEM_PROMPT  =
        "You are a helpful, concise AI assistant built with Java and the Groq API. " +
        "You run blazingly fast thanks to Groq's LPU inference engine. " +
        "Be direct and informative. If asked about your tech stack, mention: " +
        "Java 17, Spring Boot 3, Groq API, and llama3-70b-8192.";

    @Value("${groq.api.key}")           // read from application.properties / env var
    private String apiKey;

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public GroqService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
        this.mapper = new ObjectMapper();
    }

    // ── Public API ───────────────────────────────────────────────────────────
    public ChatResponse chat(ChatRequest req) {
        try {
            String model = (req.getModel() != null && !req.getModel().isBlank())
                ? req.getModel() : DEFAULT_MODEL;

            String requestBody = buildRequestBody(req, model);
            HttpResponse<String> httpResp = sendRequest(requestBody);

            if (httpResp.statusCode() != 200) {
                String errMsg = extractError(httpResp.body());
                return ChatResponse.error("Groq API error " + httpResp.statusCode() + ": " + errMsg);
            }

            return parseResponse(httpResp.body(), model);

        } catch (Exception e) {
            return ChatResponse.error("Request failed: " + e.getMessage());
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Builds the JSON request body for Groq's OpenAI-compatible endpoint.
     * Groq follows the OpenAI Chat Completions format exactly.
     */
    private String buildRequestBody(ChatRequest req, String model) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        root.put("max_tokens", 1024);
        root.put("temperature", 0.7);

        ArrayNode messages = mapper.createArrayNode();

        // 1. System message always first
        ObjectNode system = mapper.createObjectNode();
        system.put("role", "system");
        system.put("content", SYSTEM_PROMPT);
        messages.add(system);

        // 2. Conversation history (existing turns)
        List<ChatMessage> history = req.getHistory();
        if (history != null) {
            for (ChatMessage msg : history) {
                ObjectNode turn = mapper.createObjectNode();
                turn.put("role", msg.getRole());
                turn.put("content", msg.getContent());
                messages.add(turn);
            }
        }

        // 3. Current user message
        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", req.getMessage());
        messages.add(userMsg);

        root.set("messages", messages);
        return mapper.writeValueAsString(root);
    }

    /**
     * Sends the POST request to Groq using Java's built-in HttpClient (Java 11+).
     * No external HTTP library needed.
     */
    private HttpResponse<String> sendRequest(String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GROQ_API_URL))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Parses the Groq response JSON into our ChatResponse model.
     * Groq response structure mirrors OpenAI's exactly.
     */
    private ChatResponse parseResponse(String json, String model) throws Exception {
        JsonNode root = mapper.readTree(json);

        String reply = root
            .path("choices").get(0)
            .path("message")
            .path("content")
            .asText();

        JsonNode usage = root.path("usage");
        long promptTokens     = usage.path("prompt_tokens").asLong();
        long completionTokens = usage.path("completion_tokens").asLong();
        long totalTokens      = usage.path("total_tokens").asLong();

        String usedModel = root.path("model").asText(model);

        return ChatResponse.ok(reply, usedModel, promptTokens, completionTokens, totalTokens);
    }

    private String extractError(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            return root.path("error").path("message").asText("Unknown error");
        } catch (Exception e) {
            return json;
        }
    }

    // ── Available models ─────────────────────────────────────────────────────
    public List<String> getAvailableModels() {
        return List.of(
            "llama3-70b-8192",
            "llama3-8b-8192",
            "mixtral-8x7b-32768",
            "gemma2-9b-it"
        );
    }
}
