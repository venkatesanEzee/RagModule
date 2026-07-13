package org.in.com.ragModule.dto;

public record RetrievedChunk(String documentId, String fileName, int chunkIndex, double score, String content) {
}