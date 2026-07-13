package org.in.com.ragModule.dto;

public enum DocumentStatus {
	PENDING, // uploaded, not yet parsed/chunked/embedded
	PROCESSING, // parsing/chunking/embedding in progress
	COMPLETED, // fully indexed into Qdrant, ready for retrieval
	FAILED
}
