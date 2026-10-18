package com.university.knowledge.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.university.knowledge.dto.AskQuestionResponse;
import com.university.knowledge.entity.DocumentChunk;

@Service
public class RagService {

    private final SimilaritySearchService similaritySearchService;
    private final RestClient restClient;

    public RagService(
            SimilaritySearchService similaritySearchService) {

        this.similaritySearchService = similaritySearchService;

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public AskQuestionResponse ask(String question) {
        return ask(question, null, null);
    }

    /**
     * Upgraded ask method supporting optional course and semester context filtering.
     */
    public AskQuestionResponse ask(String question, String courseCode, String semester) {

        // 1. Retrieve relevant chunks with optional academic metadata filters
        List<DocumentChunk> chunks =
                similaritySearchService.search(question, 5, courseCode, semester);

        // 2. Build context from retrieved chunks
        String context = chunks.stream()
                .map(chunk ->
                        "Source: "
                                + chunk.getDocument().getTitle()
                                + (courseCode != null ? " [Course: " + courseCode + "]" : "")
                                + " (chunk "
                                + chunk.getChunkIndex()
                                + ")\n"
                                + chunk.getContent())
                .reduce("", (a, b) ->
                        a + "\n\n---\n\n" + b);

        // 3. Build the enterprise RAG prompt
        String prompt = """
                You are an advanced university academic knowledge assistant.
                Answer the user's question accurately using only the provided context.

                Rules:
                - Do not invent information or hallucinate facts outside the context.
                - If the answer is not present in the context, output exactly:
                  "The information is not available in the provided documents."
                - Keep the answer structured, clear, and professional.

                Context:
                %s

                User question:
                %s
                """.formatted(context, question);

        // 4. Send the prompt to the local Ollama model
        OllamaChatRequest request =
                new OllamaChatRequest(
                        "qwen3:4b-instruct",
                        List.of(
                                new OllamaMessage("user", prompt)
                        ),
                        false
                );

        OllamaChatResponse response =
                restClient.post()
                        .uri("/api/chat")
                        .body(request)
                        .retrieve()
                        .body(OllamaChatResponse.class);

        if (response == null || response.message() == null) {
            throw new IllegalStateException(
                    "Ollama returned no answer from local model"
            );
        }

        String answer = response.message().content();

        // 5. Return answer + source citations
        List<AskQuestionResponse.Source> sources =
                chunks.stream()
                        .map(chunk -> new AskQuestionResponse.Source(
                                chunk.getDocument().getId(),
                                chunk.getDocument().getTitle(),
                                chunk.getChunkIndex()
                        ))
                        .toList();

        return new AskQuestionResponse(answer, sources);
    }

    private record OllamaChatRequest(
            String model,
            List<OllamaMessage> messages,
            boolean stream
    ) {
    }

    private record OllamaMessage(
            String role,
            String content
    ) {
    }

    private record OllamaChatResponse(
            String model,
            OllamaMessage message,
            boolean done
    ) {
    }
}