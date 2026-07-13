package org.in.com.ragModule.dto;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class DocumentDTO {

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false)
	private String fileName;

	@Column(nullable = false)
	private String storagePath;

	@Column(nullable = false)
	private String contentType;

	@Column(nullable = false)
	private Long fileSizeBytes;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentStatus status;

	@Column(nullable = false)
	private LocalDateTime uploadedAt;

	private LocalDateTime processedAt;

	private String errorMessage;

	protected DocumentDTO() {
		// JPA requires a no-arg constructor
	}

	public DocumentDTO(String id, String fileName, String storagePath, String contentType, Long fileSizeBytes) {
		this.id = id;
		this.fileName = fileName;
		this.storagePath = storagePath;
		this.contentType = contentType;
		this.fileSizeBytes = fileSizeBytes;
		this.status = DocumentStatus.PENDING;
		this.uploadedAt = LocalDateTime.now();
	}

	// --- Getters and setters ---
	public String getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public String getContentType() {
		return contentType;
	}

	public Long getFileSizeBytes() {
		return fileSizeBytes;
	}

	public DocumentStatus getStatus() {
		return status;
	}

	public void setStatus(DocumentStatus status) {
		this.status = status;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public LocalDateTime getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(LocalDateTime processedAt) {
		this.processedAt = processedAt;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}