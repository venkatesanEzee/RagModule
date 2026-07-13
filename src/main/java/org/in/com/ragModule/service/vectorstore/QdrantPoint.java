package org.in.com.ragModule.service.vectorstore;

import java.util.List;
import java.util.Map;

public record QdrantPoint(String id, List<Float> vector, Map<String, Object> payload) {
}