package org.in.com.ragModule.service.document;

import org.in.com.ragModule.dto.DocumentDTO;
import org.in.com.ragModule.exception.FileStorageException;
import org.in.com.ragModule.exception.UnsupportedFileTypeException;
import org.in.com.ragModule.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentStorageService {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("application/pdf",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
			"text/plain");

	private final DocumentRepository documentRepository;
	private final Path storageRoot;

	public DocumentStorageService(DocumentRepository documentRepository,
			@Value("${storage.root-path:./uploaded-documents}") String storageRootPath) {
		this.documentRepository = documentRepository;
		this.storageRoot = Paths.get(storageRootPath).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.storageRoot);
		} catch (IOException e) {
			throw new FileStorageException("Could not create storage directory", e);
		}
	}

	public DocumentDTO store(MultipartFile file) {
		validateFileType(file);

		String documentId = UUID.randomUUID().toString();
		String originalFileName = sanitizeFileName(file.getOriginalFilename());
		String storedFileName = documentId + "_" + originalFileName;
		Path targetPath = storageRoot.resolve(storedFileName);

		try {
			Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new FileStorageException("Failed to store file: " + originalFileName, e);
		}

		DocumentDTO document = new DocumentDTO(documentId, originalFileName, targetPath.toString(),
				file.getContentType(), file.getSize());

		return documentRepository.save(document);
	}

	private void validateFileType(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new UnsupportedFileTypeException(
					"Unsupported file type: " + contentType + ". Allowed: PDF, DOCX, TXT");
		}
	}

	private String sanitizeFileName(String originalFileName) {
		if (originalFileName == null) {
			return "unnamed";
		}
		// Strip path components — prevents directory traversal like "../../evil.sh"
		return Paths.get(originalFileName).getFileName().toString();
	}
}