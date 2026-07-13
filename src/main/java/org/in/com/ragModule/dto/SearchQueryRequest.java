package org.in.com.ragModule.dto;

import jakarta.validation.constraints.NotBlank;

public record SearchQueryRequest(@NotBlank(message = "query must not be blank") String query, Integer limit,
		String namespace) {
}