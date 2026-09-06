package com.example.aiassistant.dto;

import jakarta.validation.constraints.NotBlank;

public class Message {

    @NotBlank(message = "Role cannot be blank (e.g., 'user' or 'assistant')")
    private String role;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    public Message() {
    }

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
