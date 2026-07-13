package org.in.com.ragModule.service.vectorstore;

import java.util.List;
import java.util.Map;

public record SearchResponse(List<SearchResult> result) {
	public record SearchResult(String id, double score, Map<String, Object> payload) {
	}
}