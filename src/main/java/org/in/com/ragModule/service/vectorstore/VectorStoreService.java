package org.in.com.ragModule.service.vectorstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class VectorStoreService {

	private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);
	private static final int EMBEDDING_DIMENSIONS = 768; // must match nomic-embed-text's output size

	private final RestClient qdrantRestClient;
	private final String collectionName;

	public VectorStoreService(RestClient qdrantRestClient, @Value("${qdrant.collection-name}") String collectionName) {
		this.qdrantRestClient = qdrantRestClient;
		this.collectionName = collectionName;
	}

	/**
	 * Runs once at application startup. Creates the collection if it doesn't exist.
	 * Safe to call repeatedly — Qdrant returns a 409-ish "already exists" style
	 * response that we treat as a no-op, not an error.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void ensureCollectionExists() {
		boolean exists = collectionExists();
		if (exists) {
			log.info("Qdrant collection '{}' already exists — skipping creation", collectionName);
			return;
		}

		CreateCollectionRequest request = new CreateCollectionRequest(new VectorConfig(EMBEDDING_DIMENSIONS, "Cosine"));

		qdrantRestClient.put().uri("/collections/{name}", collectionName).body(request).retrieve().toBodilessEntity();

		log.info("Created Qdrant collection '{}' with {} dimensions, Cosine distance", collectionName,
				EMBEDDING_DIMENSIONS);

		createPayloadIndex("documentId");
		createPayloadIndex("namespace");
	}

	private boolean collectionExists() {
		try {
			qdrantRestClient.get().uri("/collections/{name}", collectionName).retrieve().toBodilessEntity();
			return true;
		} catch (HttpClientErrorException.NotFound e) {
			return false;
		}
	}

	/**
	 * Indexes a payload field so filtered searches (e.g. "only within this
	 * namespace") stay fast as the collection grows, instead of scanning every
	 * point's payload.
	 */
	private void createPayloadIndex(String fieldName) {
		qdrantRestClient.put().uri("/collections/{name}/index", collectionName)
				.body(new java.util.HashMap<>(Map.of("field_name", fieldName, "field_schema", "keyword"))).retrieve()
				.toBodilessEntity();
		log.info("Created payload index on field '{}'", fieldName);
	}

	public void upsertPoints(List<QdrantPoint> points) {
		if (points.isEmpty()) {
			return;
		}

		UpsertPointsRequest request = new UpsertPointsRequest(points);

		qdrantRestClient.put().uri("/collections/{name}/points", collectionName).body(request).retrieve()
				.toBodilessEntity();

		log.info("Upserted {} points into collection '{}'", points.size(), collectionName);
	}

	public List<SearchResponse.SearchResult> search(List<Float> queryVector, int limit, String namespace) {
		SearchFilter filter = new SearchFilter(List.of(new FilterCondition("namespace", new MatchValue(namespace))));

		SearchRequest request = new SearchRequest(queryVector, limit, true, filter);

		SearchResponse response = qdrantRestClient.post().uri("/collections/{name}/points/search", collectionName)
				.body(request).retrieve().body(SearchResponse.class);

		if (response == null || response.result() == null) {
			return List.of();
		}

		log.info("Search returned {} results, top score: {}", response.result().size(),
				response.result().isEmpty() ? "N/A" : response.result().get(0).score());

		return response.result();
	}
}