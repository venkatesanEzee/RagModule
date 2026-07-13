package org.in.com.ragModule.service.chat;

import java.util.List;
import java.util.Map;

public record OllamaChatRequest(
		String model,
		List<OllamaMessage> messages,
		boolean stream,
		Boolean think,
		Map<String, Object> options) {
}