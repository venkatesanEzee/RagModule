package org.in.com.ragModule.service.chunking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

	// Priority order: try splitting on paragraph breaks first, then lines,
	// then sentences, then words, then finally give up and cut mid-word.
	private static final String[] SEPARATORS = { "\n\n", "\n", ". ", " ", "" };

	private final int chunkSize;
	private final int chunkOverlap;

	public ChunkingService(@Value("${chunking.chunk-size:1000}") int chunkSize,
			@Value("${chunking.chunk-overlap:150}") int chunkOverlap) {
		this.chunkSize = chunkSize;
		this.chunkOverlap = chunkOverlap;
	}

	public List<Chunk> chunk(String text) {
		if (text == null || text.isBlank()) {
			return List.of();
		}

		List<String> rawPieces = splitRecursively(text.strip(), 0);
		return mergeWithOverlap(rawPieces, text);
	}

	/**
	 * Recursively splits text using the separator priority list. If a piece is
	 * still too big after splitting on the current separator, we recurse into it
	 * using the NEXT separator in the list.
	 */
	private List<String> splitRecursively(String text, int separatorIndex) {
		if (text.length() <= chunkSize) {
			return List.of(text);
		}

		if (separatorIndex >= SEPARATORS.length) {
			// Last resort: hard cut at chunkSize, no separator worked
			return hardSplit(text);
		}

		String separator = SEPARATORS[separatorIndex];
		List<String> pieces;

		if (separator.isEmpty()) {
			return hardSplit(text);
		} else {
			String[] split = text.split(java.util.regex.Pattern.quote(separator), -1);
			pieces = new ArrayList<>(List.of(split));
		}

		// Re-merge small pieces up to chunkSize, and recurse into any piece still too
		// large
		List<String> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();

		for (String piece : pieces) {
			String candidate = current.isEmpty() ? piece : current + separator + piece;

			if (candidate.length() <= chunkSize) {
				current = new StringBuilder(candidate);
			} else {
				if (!current.isEmpty()) {
					result.add(current.toString());
					current = new StringBuilder();
				}
				if (piece.length() > chunkSize) {
					// This single piece is still too big — recurse with the next separator
					result.addAll(splitRecursively(piece, separatorIndex + 1));
				} else {
					current = new StringBuilder(piece);
				}
			}
		}
		if (!current.isEmpty()) {
			result.add(current.toString());
		}

		return result;
	}

	private List<String> hardSplit(String text) {
		List<String> pieces = new ArrayList<>();
		for (int i = 0; i < text.length(); i += chunkSize) {
			pieces.add(text.substring(i, Math.min(i + chunkSize, text.length())));
		}
		return pieces;
	}

	/**
	 * Takes clean, non-overlapping pieces and stitches overlap between consecutive
	 * chunks by borrowing the tail of the previous piece.
	 */
	private List<Chunk> mergeWithOverlap(List<String> pieces, String originalText) {
		List<Chunk> chunks = new ArrayList<>();
		int searchFrom = 0;

		for (int i = 0; i < pieces.size(); i++) {
			String content = pieces.get(i);

			if (i > 0 && chunkOverlap > 0) {
				String previous = pieces.get(i - 1);
				String overlapText = previous.length() > chunkOverlap
						? previous.substring(previous.length() - chunkOverlap)
						: previous;
				content = overlapText + content;
			}

			int start = originalText.indexOf(pieces.get(i), searchFrom);
			if (start == -1)
				start = searchFrom; // fallback if exact match not found (whitespace normalization edge cases)
			int end = start + pieces.get(i).length();
			searchFrom = start;

			chunks.add(new Chunk(i, content.strip(), start, end));
		}

		return chunks;
	}
}