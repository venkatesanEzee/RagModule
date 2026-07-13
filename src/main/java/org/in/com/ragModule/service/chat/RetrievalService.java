package org.in.com.ragModule.service.chat;

import org.in.com.ragModule.dto.RetrievedChunk;
import org.in.com.ragModule.service.embedding.EmbeddingService;
import org.in.com.ragModule.service.vectorstore.SearchResponse;
import org.in.com.ragModule.service.vectorstore.VectorStoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetrievalService {

	private final EmbeddingService embeddingService;
	private final VectorStoreService vectorStoreService;

	public RetrievalService(EmbeddingService embeddingService, VectorStoreService vectorStoreService) {
		this.embeddingService = embeddingService;
		this.vectorStoreService = vectorStoreService;
	}

	public List<RetrievedChunk> retrieve(String query, int limit, String namespace) {
		List<Float> queryVector = embeddingService.embed(query);
		List<SearchResponse.SearchResult> results = vectorStoreService.search(queryVector, limit, namespace);

		return results.stream().map(this::toRetrievedChunk).toList();
	}

	private RetrievedChunk toRetrievedChunk(SearchResponse.SearchResult result) {
		var payload = result.payload();
		return new RetrievedChunk((String) payload.get("documentId"), (String) payload.get("fileName"),
				(Integer) payload.get("chunkIndex"), result.score(), (String) payload.get("content"));
	}
}