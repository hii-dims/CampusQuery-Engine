package com.university.knowledge.service;

import com.university.knowledge.dto.AskQuestionResponse;
import com.university.knowledge.entity.ConversationMessage;
import com.university.knowledge.entity.DocumentChunk;
import com.university.knowledge.repository.ConversationMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class RagService {

    private final SimilaritySearchService similaritySearchService;
    private final ConversationMessageRepository conversationMessageRepository;
    private final RestClient restClient;

    public RagService(
            SimilaritySearchService similaritySearchService,
            ConversationMessageRepository conversationMessageRepository) {

        this.similaritySearchService = similaritySearchService;
        this.conversationMessageRepository =
                conversationMessageRepository;

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public AskQuestionResponse ask(String question) {

        conversationMessageRepository.save(
                new ConversationMessage("user", question)
        );

        List<DocumentChunk> chunks =
                similaritySearchService.search(question, 5);

        String context = chunks.stream()
                .map(chunk ->
                        "Source: "
                                + chunk.getDocument().getTitle()
                                + " (chunk "
                                + chunk.getChunkIndex()
                                + ")\n"
                                + chunk.getContent())
                .reduce("", (a, b) ->
                        a + "\n\n---\n\n" + b);

        String prompt = """
                You are a university knowledge assistant.

                Answer the user's question using only the provided context.

                Rules:
                - Do not invent information.
                - If the answer is not present in the context, say:
                  "The information is not available in the provided documents."
                - Keep the answer clear and concise.
                - Use the source information when relevant.

                Context:
                %s

                User question:
                %s
                """.formatted(context, question);

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
                    "Ollama returned no answer"
            );
        }

        String answer = response.message().content();

        conversationMessageRepository.save(
                new ConversationMessage("assistant", answer)
        );

        List<AskQuestionResponse.Source> sources =
                chunks.stream()
                        .map(chunk -> new AskQuestionResponse.Source(
                                chunk.getDocument().getId(),
                                chunk.getDocument().getTitle(),
                                chunk.getDocument().getFileName(),
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
