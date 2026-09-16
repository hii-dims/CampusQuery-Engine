package com.university.knowledge.service;

import com.university.knowledge.entity.DocumentChunk;
import com.university.knowledge.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        var embedding =
                embeddingService.generateEmbedding(chunk.getContent());

        DocumentChunk savedChunk =
                documentChunkRepository.save(chunk);

        String vectorLiteral = embedding.toString();

        documentChunkRepository.updateEmbedding(
                savedChunk.getId(),
                vectorLiteral
        );

        return savedChunk;
    }
}
