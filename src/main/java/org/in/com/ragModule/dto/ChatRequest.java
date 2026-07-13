package org.in.com.ragModule.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank(message = "question must not be blank") String question, Integer limit,
		String namespace) {
}