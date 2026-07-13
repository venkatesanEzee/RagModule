package org.in.com.ragModule.sqlsource;

import org.in.com.ragModule.service.chunking.Chunk;
import org.in.com.ragModule.service.chunking.ChunkingService;
import org.in.com.ragModule.service.embedding.EmbeddingService;
import org.in.com.ragModule.service.vectorstore.QdrantPoint;
import org.in.com.ragModule.service.vectorstore.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SqlIngestionService {

	private static final Logger log = LoggerFactory.getLogger(SqlIngestionService.class);
	private static final int BATCH_SIZE = 50;

	private final SqlDataSourceService sqlDataSourceService;
	private final ChunkingService chunkingService;
	private final EmbeddingService embeddingService;
	private final VectorStoreService vectorStoreService;

	public SqlIngestionService(SqlDataSourceService sqlDataSourceService, ChunkingService chunkingService,
			EmbeddingService embeddingService, VectorStoreService vectorStoreService) {
		this.sqlDataSourceService = sqlDataSourceService;
		this.chunkingService = chunkingService;
		this.embeddingService = embeddingService;
		this.vectorStoreService = vectorStoreService;
	}

	@Async
	public void ingestTableAsync(String tableName) {
		List<String> batch = new ArrayList<>(BATCH_SIZE);
		AtomicInteger totalIndexed = new AtomicInteger(0);

		sqlDataSourceService.streamTableAsSentences(tableName, sentence -> {
			batch.add(sentence);
			if (batch.size() >= BATCH_SIZE) {
				indexBatch(tableName, batch, totalIndexed);
				batch.clear();
			}
		});

		if (!batch.isEmpty()) {
			indexBatch(tableName, batch, totalIndexed);
		}

		log.info("Finished ingesting table '{}': {} chunks indexed", tableName, totalIndexed.get());
	}

	private void indexBatch(String tableName, List<String> sentences, AtomicInteger totalIndexed) {
		String batchText = String.join("\n\n", sentences);
		List<Chunk> chunks = chunkingService.chunk(batchText);

		List<QdrantPoint> points = new ArrayList<>();
		for (Chunk chunk : chunks) {
			List<Float> vector = embeddingService.embed(chunk.content());
			String pointId = UUID
					.nameUUIDFromBytes(
							("sql_" + tableName + "_" + totalIndexed.get() + "_" + chunk.chunkIndex()).getBytes())
					.toString();

			Map<String, Object> payload = Map.of("documentId", "sql_" + tableName, "chunkIndex",
					totalIndexed.get() + chunk.chunkIndex(), "fileName", tableName + " (SQL table)", "namespace",
					"default", "content", chunk.content());
			points.add(new QdrantPoint(pointId, vector, payload));
		}

		vectorStoreService.upsertPoints(points);
		totalIndexed.addAndGet(points.size());
		log.info("Indexed batch for '{}' — running total: {}", tableName, totalIndexed.get());
	}
}