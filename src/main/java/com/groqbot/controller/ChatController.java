package com.groqbot.controller;

import com.groqbot.model.ChatRequest;
import com.groqbot.model.ChatResponse;
import com.groqbot.service.GroqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")   // allow frontend dev server calls
public class ChatController {

    @Autowired
    private GroqService groqService;

    /**
     * POST /api/chat
     * Body: { message, history[], model? }
     * Returns: { reply, model, tokens, success }
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest()
                .body(ChatResponse.error("Message cannot be empty"));
        }
        ChatResponse response = groqService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/models
     * Returns list of available Groq models
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, List<String>>> getModels() {
        return ResponseEntity.ok(Map.of("models", groqService.getAvailableModels()));
    }

    /**
     * GET /api/health
     * Simple health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "groq-chatbot"));
    }
}
