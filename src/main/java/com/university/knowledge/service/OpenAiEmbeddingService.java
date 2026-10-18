package com.university.knowledge.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenAiEmbeddingService {

    private final RestClient restClient;

    public OpenAiEmbeddingService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    /**
     * Generates text embeddings with Spring caching enabled.
     * Repeated embedding requests for the same text will load instantly from memory.
     */
    @Cacheable(value = "embeddings", key = "#text")
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