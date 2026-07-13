package org.in.com.ragModule.service.vectorstore;

import java.util.List;

public record SearchRequest(List<Float> vector, int limit, boolean withPayload, SearchFilter filter) {
}