package org.in.com.ragModule.service.chat;

import org.in.com.ragModule.dto.RetrievedChunk;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptService {

	private static final String SYSTEM_INSTRUCTION =
			"Answer using ONLY the context below. Cite source numbers. If context is insufficient, say so.";

	public String buildPrompt(String question, List<RetrievedChunk> retrievedChunks) {
		StringBuilder prompt = new StringBuilder();
		prompt.append(SYSTEM_INSTRUCTION).append("\n\n");

		if (retrievedChunks.isEmpty()) {
			prompt.append("CONTEXT: (none)\n\n");
		} else {
			prompt.append("CONTEXT:\n");
			for (int i = 0; i < retrievedChunks.size(); i++) {
				RetrievedChunk chunk = retrievedChunks.get(i);
				prompt.append("[").append(i + 1).append("] ").append(chunk.fileName()).append(":\n");
				prompt.append(chunk.content()).append("\n\n");
			}
		}

		prompt.append("QUESTION: ").append(question);
		return prompt.toString();
	}
}