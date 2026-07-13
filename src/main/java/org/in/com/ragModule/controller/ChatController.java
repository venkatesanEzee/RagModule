package org.in.com.ragModule.controller;

import java.util.List;
import java.util.Map;

import org.in.com.ragModule.dto.ChatRequest;
import org.in.com.ragModule.dto.ChatResponse;
import org.in.com.ragModule.dto.RetrievedChunk;
import org.in.com.ragModule.service.chat.ChatService;
import org.in.com.ragModule.service.chat.PromptService;
import org.in.com.ragModule.service.chat.RetrievalService;
import org.in.com.ragModule.sqlsource.SqlIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/chat")
public class ChatController {

	private static final int DEFAULT_LIMIT = 3;
	private static final String DEFAULT_NAMESPACE = "default";

	private final RetrievalService retrievalService;
	private final PromptService promptService;
	private final ChatService chatService;
	private final SqlIngestionService sqlIngestionService;

	public ChatController(RetrievalService retrievalService, PromptService promptService, ChatService chatService,
			SqlIngestionService sqlIngestionService) {
		this.retrievalService = retrievalService;
		this.promptService = promptService;
		this.chatService = chatService;
		this.sqlIngestionService = sqlIngestionService;
	}

	@PostMapping
	public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
		int limit = request.limit() != null ? request.limit() : DEFAULT_LIMIT;
		String namespace = request.namespace() != null ? request.namespace() : DEFAULT_NAMESPACE;

		List<RetrievedChunk> sources = retrievalService.retrieve(request.question(), limit, namespace);
		String prompt = promptService.buildPrompt(request.question(), sources);
		String answer = chatService.generateAnswer(prompt);

		return ResponseEntity.ok(new ChatResponse(request.question(), answer, sources));
	}

	@PostMapping("/sql-source/{tableName}/ingest")
	public ResponseEntity<Map<String, String>> ingestSqlTable(@PathVariable String tableName) {
		sqlIngestionService.ingestTableAsync(tableName);
		return ResponseEntity.accepted().body(
				Map.of("message", "Ingestion started in background for table: " + tableName, "status", "PROCESSING"));
	}
}