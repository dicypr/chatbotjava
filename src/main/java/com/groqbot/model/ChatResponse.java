package com.groqbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private String model;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private boolean success;
    private String error;

    public static ChatResponse ok(String reply, String model,
                                   long prompt, long completion, long total) {
        return new ChatResponse(reply, model, prompt, completion, total, true, null);
    }

    public static ChatResponse error(String message) {
        return new ChatResponse(null, null, 0, 0, 0, false, message);
    }
}
