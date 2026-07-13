package org.in.com.ragModule.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.in.com.ragModule.exception.DocumentParsingException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DocumentParsingService {

	private static final Logger log = LoggerFactory.getLogger(DocumentParsingService.class);

	// -1 = no limit on extracted characters. Default Tika limit is 100,000 — too
	// small for real docs.
	private static final int NO_WRITE_LIMIT = -1;

	public String extractText(Path filePath) {
		log.info("Parsing document: {}", filePath.getFileName());

		AutoDetectParser parser = new AutoDetectParser();
		BodyContentHandler handler = new BodyContentHandler(NO_WRITE_LIMIT);
		Metadata metadata = new Metadata();
		ParseContext context = new ParseContext();

		try (InputStream stream = Files.newInputStream(filePath)) {
			parser.parse(stream, handler, metadata, context);
			String extractedText = handler.toString();

			if (extractedText == null || extractedText.isBlank()) {
				log.warn("No text extracted from {} — possibly a scanned/image-only PDF", filePath.getFileName());
			}

			log.info("Extracted {} characters from {}", extractedText.length(), filePath.getFileName());
			return extractedText;

		} catch (IOException | TikaException | org.xml.sax.SAXException e) {
			throw new DocumentParsingException("Failed to parse document: " + filePath.getFileName(), e);
		}
	}
}