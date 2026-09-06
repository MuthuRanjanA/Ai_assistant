package com.example.aiassistant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ChatRequest {

    @NotEmpty(message = "Messages list cannot be empty")
    @Valid
    private List<Message> messages;

    public ChatRequest() {
    }

    public ChatRequest(List<Message> messages) {
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
