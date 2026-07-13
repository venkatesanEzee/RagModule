package org.in.com.ragModule.io;

import java.util.List;

import org.in.com.ragModule.dto.RetrievedChunk;

public record SearchQueryResponse(String query, List<RetrievedChunk> results) {
}