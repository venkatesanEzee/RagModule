package org.in.com.ragModule.service.embedding;

import org.in.com.ragModule.exception.EmbeddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class EmbeddingService {

	private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

	private final RestClient ollamaRestClient;
	private final String embeddingModel;

	public EmbeddingService(RestClient ollamaRestClient, @Value("${ollama.embedding-model}") String embeddingModel) {
		this.ollamaRestClient = ollamaRestClient;
		this.embeddingModel = embeddingModel;
	}

	public List<Float> embed(String text) {
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("Cannot embed blank text");
		}

		OllamaEmbeddingRequest request = new OllamaEmbeddingRequest(embeddingModel, text);

		long start = System.currentTimeMillis();

		OllamaEmbeddingResponse response = ollamaRestClient.post().uri("/api/embeddings").body(request).retrieve()
				.body(OllamaEmbeddingResponse.class);

		long elapsedMs = System.currentTimeMillis() - start;

		if (response == null || response.embedding() == null || response.embedding().isEmpty()) {
			throw new EmbeddingException("Ollama returned an empty embedding for text of length " + text.length());
		}

		log.debug("Embedded {} chars in {} ms -> vector size {}", text.length(), elapsedMs,
				response.embedding().size());

		// Ollama returns Double; Qdrant client (Phase 6) expects Float. Convert once,
		// here.
		return response.embedding().stream().map(Double::floatValue).toList();
	}

	public List<List<Float>> embedBatch(List<String> texts) {
		// Ollama's /api/embeddings endpoint handles one text at a time — no native
		// batch endpoint
		// in this API version. We loop here. This is a known performance bottleneck
		// we'll
		// address with concurrency in Phase 10.
		return texts.stream().map(this::embed).toList();
	}
}