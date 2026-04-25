package com.groqbot.model;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private String message;                  // latest user message
    private List<ChatMessage> history;       // full conversation history
    private String model;                    // optional model override
}
