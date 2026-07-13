package org.in.com.ragModule.io;

import java.time.LocalDateTime;

import org.in.com.ragModule.dto.DocumentStatus;

public record DocumentUploadResponse(String documentId, String fileName, DocumentStatus status,
		LocalDateTime uploadedAt) {
}