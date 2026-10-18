package com.university.knowledge.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.knowledge.entity.DocumentChunk;
import com.university.knowledge.repository.DocumentChunkRepository;

@Service
public class DocumentEmbeddingService {

    private final DocumentChunkRepository documentChunkRepository;
    private final OpenAiEmbeddingService embeddingService;

    public DocumentEmbeddingService(
            DocumentChunkRepository documentChunkRepository,
            OpenAiEmbeddingService embeddingService) {

        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public DocumentChunk generateAndStoreEmbedding(DocumentChunk chunk) {

        // Generate the 768-dimensional embedding using Ollama
        var embedding =
                embeddingService.generateEmbedding(chunk.getContent());

        // Save the chunk without the embedding column
        DocumentChunk savedChunk =
                documentChunkRepository.save(chunk);

        // Convert the embedding to PostgreSQL vector literal format
        String vectorLiteral = embedding.toString();

        // Store the vector directly in PostgreSQL
        documentChunkRepository.updateEmbedding(
                savedChunk.getId(),
                vectorLiteral
        );

        return savedChunk;
    }
}