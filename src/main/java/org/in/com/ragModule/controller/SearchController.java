package org.in.com.ragModule.controller;

import jakarta.validation.Valid;

import org.in.com.ragModule.dto.RetrievedChunk;
import org.in.com.ragModule.dto.SearchQueryRequest;
import org.in.com.ragModule.io.SearchQueryResponse;
import org.in.com.ragModule.service.chat.PromptService;
import org.in.com.ragModule.service.chat.RetrievalService;
import org.in.com.ragModule.service.embedding.EmbeddingService;
import org.in.com.ragModule.service.vectorstore.SearchResponse;
import org.in.com.ragModule.service.vectorstore.VectorStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {
	private static final int DEFAULT_LIMIT = 5;
	private static final String DEFAULT_NAMESPACE = "default";

	private final RetrievalService retrievalService;
	private final PromptService promptService;

	public SearchController(RetrievalService retrievalService, PromptService promptService) {
		this.retrievalService = retrievalService;
		this.promptService = promptService;
	}

	@PostMapping
	public ResponseEntity<SearchQueryResponse> search(@Valid @RequestBody SearchQueryRequest request) {
		int limit = request.limit() != null ? request.limit() : DEFAULT_LIMIT;
		String namespace = request.namespace() != null ? request.namespace() : DEFAULT_NAMESPACE;

		List<RetrievedChunk> chunks = retrievalService.retrieve(request.query(), limit, namespace);

		return ResponseEntity.ok(new SearchQueryResponse(request.query(), chunks));
	}

	@PostMapping("/debug-prompt")
	public ResponseEntity<Map<String, String>> debugPrompt(@RequestBody SearchQueryRequest request) {
		int limit = request.limit() != null ? request.limit() : DEFAULT_LIMIT;
		String namespace = request.namespace() != null ? request.namespace() : DEFAULT_NAMESPACE;

		List<RetrievedChunk> chunks = retrievalService.retrieve(request.query(), limit, namespace);
		String prompt = promptService.buildPrompt(request.query(), chunks);

		return ResponseEntity.ok(Map.of("prompt", prompt));
	}
}