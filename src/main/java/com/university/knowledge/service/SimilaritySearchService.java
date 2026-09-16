package com.university.knowledge.service;

import com.university.knowledge.entity.DocumentChunk;
import com.university.knowledge.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimilaritySearchService {

    private final DocumentChunkRepository documentChunkRepository;
    private final OpenAiEmbeddingService embeddingService;

    public SimilaritySearchService(
            DocumentChunkRepository documentChunkRepository,
            OpenAiEmbeddingService embeddingService) {

        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
    }

    public List<DocumentChunk> search(String question, int limit) {

        List<Float> embedding =
                embeddingService.generateEmbedding(question);

        String vectorLiteral = embedding.toString();

        return documentChunkRepository.findSimilarChunks(
                vectorLiteral,
                limit
        );
    }
}
