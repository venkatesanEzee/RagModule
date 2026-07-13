package org.in.com.ragModule.service.chat;

public record OllamaChatResponse(String model, OllamaMessage message, boolean done) {
}