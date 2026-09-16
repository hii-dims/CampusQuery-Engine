package com.university.knowledge.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OpenAiEmbeddingService {

    private final RestClient restClient;

    public OpenAiEmbeddingService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public List<Float> generateEmbedding(String text) {

        OllamaEmbeddingRequest request =
                new OllamaEmbeddingRequest(
                        "nomic-embed-text",
                        text
                );

        OllamaEmbeddingResponse response =
                restClient.post()
                        .uri("/api/embed")
                        .body(request)
                        .retrieve()
                        .body(OllamaEmbeddingResponse.class);

        if (response == null
                || response.embeddings() == null
                || response.embeddings().isEmpty()) {

            throw new IllegalStateException(
                    "Ollama returned no embedding"
            );
        }

        return response.embeddings().get(0);
    }

    private record OllamaEmbeddingRequest(
            String model,
            String input
    ) {
    }

    private record OllamaEmbeddingResponse(
            List<List<Float>> embeddings
    ) {
    }
}
