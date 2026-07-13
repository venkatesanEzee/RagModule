package org.in.com.ragModule.controller;

import org.in.com.ragModule.dto.DocumentDTO;
import org.in.com.ragModule.repository.DocumentRepository;
import org.in.com.ragModule.service.DocumentParsingService;
import org.in.com.ragModule.service.chunking.Chunk;
import org.in.com.ragModule.service.chunking.ChunkingService;
import org.in.com.ragModule.service.embedding.EmbeddingService;
import org.in.com.ragModule.service.vectorstore.QdrantPoint;
import org.in.com.ragModule.service.vectorstore.VectorStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * TEMPORARY debug controller for Phase 3. We'll delete this once parsing is
 * wired into the real ingestion pipeline (Phase 6+).
 */
@RestController
public class DebugParsingController {

	private final DocumentRepository documentRepository;
	private final DocumentParsingService parsingService;
	private final ChunkingService chunkingService;
	private final EmbeddingService embeddingService;
	private final VectorStoreService vectorStoreService;

	public DebugParsingController(DocumentRepository documentRepository, DocumentParsingService parsingService,
			ChunkingService chunkingService, EmbeddingService embeddingService, VectorStoreService vectorStoreService) {
		this.documentRepository = documentRepository;
		this.parsingService = parsingService;
		this.chunkingService = chunkingService;
		this.embeddingService = embeddingService;
		this.vectorStoreService = vectorStoreService;
	}

	@GetMapping("/documents/{id}/debug-parse")
	public ResponseEntity<String> debugParse(@PathVariable String id) {
		DocumentDTO document = documentRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Document not found: " + id));

		String text = parsingService.extractText(Path.of(document.getStoragePath()));
		return ResponseEntity.ok(text);
	}

	@GetMapping("/documents/{id}/debug-chunks")
	public ResponseEntity<List<Chunk>> debugChunks(@PathVariable String id) {
		DocumentDTO document = documentRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Document not found: " + id));

		String text = parsingService.extractText(Path.of(document.getStoragePath()));
		List<Chunk> chunks = chunkingService.chunk(text);
		return ResponseEntity.ok(chunks);
	}

	@GetMapping("/documents/{id}/debug-embedding")
	public ResponseEntity<Map<String, Object>> debugEmbedding(@PathVariable String id) {
		DocumentDTO document = documentRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Document not found: " + id));

		String text = parsingService.extractText(Path.of(document.getStoragePath()));
		List<Chunk> chunks = chunkingService.chunk(text);

		if (chunks.isEmpty()) {
			return ResponseEntity.ok(Map.of("message", "No chunks to embed"));
		}

		// Just embed the first chunk for this debug check
		List<Float> vector = embeddingService.embed(chunks.get(0).content());

		return ResponseEntity.ok(Map.of("chunkContent", chunks.get(0).content(), "vectorDimensions", vector.size(),
				"firstFiveValues", vector.subList(0, Math.min(5, vector.size()))));
	}

	@GetMapping("/documents/{id}/debug-index")
	public ResponseEntity<Map<String, Object>> debugIndex(@PathVariable String id) {
		DocumentDTO document = documentRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Document not found: " + id));

		String text = parsingService.extractText(Path.of(document.getStoragePath()));
		List<Chunk> chunks = chunkingService.chunk(text);

		List<QdrantPoint> points = new ArrayList<>();
		for (Chunk chunk : chunks) {
			List<Float> vector = embeddingService.embed(chunk.content());

			String pointId = java.util.UUID.nameUUIDFromBytes((document.getId() + "_" + chunk.chunkIndex()).getBytes())
					.toString();

			Map<String, Object> payload = Map.of("documentId", document.getId(), "chunkIndex", chunk.chunkIndex(),
					"fileName", document.getFileName(), "namespace", "default", "content", chunk.content());

			points.add(new QdrantPoint(pointId, vector, payload));
		}

		vectorStoreService.upsertPoints(points);

		return ResponseEntity.ok(Map.of("documentId", document.getId(), "chunksIndexed", points.size()));
	}
}