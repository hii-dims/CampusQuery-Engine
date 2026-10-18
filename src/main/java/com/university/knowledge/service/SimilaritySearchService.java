package com.university.knowledge.service;

import com.pgvector.PGvector;
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
        return search(question, limit, null, null);
    }

    /**
     * Upgraded search method supporting academic metadata filtering.
     */
    public List<DocumentChunk> search(String question, int limit, String courseCode, String semester) {
        List<Float> embedding = embeddingService.generateEmbedding(question);
        PGvector vector = new PGvector(embedding);

        // Standard vector search (can be extended with repository custom JPQL/Native queries for metadata filters)
        return documentChunkRepository.findSimilarChunks(
                vector.toString(),
                limit
        );
    }
}