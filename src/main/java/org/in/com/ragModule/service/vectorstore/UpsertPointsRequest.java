package org.in.com.ragModule.service.vectorstore;

import java.util.List;

public record UpsertPointsRequest(List<QdrantPoint> points) {
}