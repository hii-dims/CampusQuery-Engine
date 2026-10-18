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

        List<Float> embedding =
                embeddingService.generateEmbedding(question);

        PGvector vector = new PGvector(embedding);

        return documentChunkRepository.findSimilarChunks(
                vector.toString(),
                limit
        );
    }
}