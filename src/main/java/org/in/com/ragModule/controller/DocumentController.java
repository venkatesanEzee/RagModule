package org.in.com.ragModule.controller;

import org.in.com.ragModule.dto.DocumentDTO;
import org.in.com.ragModule.io.DocumentUploadResponse;
import org.in.com.ragModule.service.document.DocumentStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
public class DocumentController {

	private final DocumentStorageService storageService;

	public DocumentController(DocumentStorageService storageService) {
		this.storageService = storageService;
	}

	@PostMapping(value = "/upload", consumes = "multipart/form-data")
	public ResponseEntity<DocumentUploadResponse> upload(@RequestParam("file") MultipartFile file) {
		DocumentDTO saved = storageService.store(file);

		DocumentUploadResponse response = new DocumentUploadResponse(saved.getId(), saved.getFileName(),
				saved.getStatus(), saved.getUploadedAt());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}