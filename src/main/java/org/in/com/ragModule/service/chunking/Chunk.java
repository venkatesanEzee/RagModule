package org.in.com.ragModule.service.chunking;

public record Chunk(int chunkIndex, String content, int startOffset, int endOffset) {
}