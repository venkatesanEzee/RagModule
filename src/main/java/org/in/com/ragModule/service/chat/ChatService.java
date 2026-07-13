package org.in.com.ragModule.service.chat;

import org.in.com.ragModule.exception.ChatGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);

	private final RestClient ollamaRestClient;
	private final String chatModel;
	private final int numPredict;
	private final int numCtx;

	public ChatService(
			RestClient ollamaRestClient,
			@Value("${ollama.chat-model}") String chatModel,
			@Value("${ollama.num-predict:512}") int numPredict,
			@Value("${ollama.num-ctx:2048}") int numCtx) {
		this.ollamaRestClient = ollamaRestClient;
		this.chatModel = chatModel;
		this.numPredict = numPredict;
		this.numCtx = numCtx;
	}

	public String generateAnswer(String prompt) {
		Map<String, Object> options = Map.of(
				"num_predict", numPredict,   // max tokens to generate
				"num_ctx", numCtx,           // context window size
				"temperature", 0.1           // lower = more focused/faster
		);

		OllamaChatRequest request = new OllamaChatRequest(
				chatModel,
				List.of(new OllamaMessage("user", prompt)),
				false,
				false,
				options);

		long start = System.currentTimeMillis();

		OllamaChatResponse response = ollamaRestClient.post()
				.uri("/api/chat")
				.body(request)
				.retrieve()
				.body(OllamaChatResponse.class);

		long elapsedMs = System.currentTimeMillis() - start;

		if (response == null || response.message() == null || response.message().content() == null) {
			throw new ChatGenerationException("Ollama returned an empty chat response");
		}

		log.info("Chat generation took {} ms, response length {} chars", elapsedMs,
				response.message().content().length());

		return response.message().content();
	}
}