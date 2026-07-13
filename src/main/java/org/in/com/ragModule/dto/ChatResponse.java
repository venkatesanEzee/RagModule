package org.in.com.ragModule.dto;

import java.util.List;

public record ChatResponse(String question, String answer, List<RetrievedChunk> sources) {
}