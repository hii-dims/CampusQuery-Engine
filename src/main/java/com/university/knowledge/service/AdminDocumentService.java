package com.university.knowledge.service;

import com.university.knowledge.entity.Document;
import com.university.knowledge.repository.DocumentChunkRepository;
import com.university.knowledge.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public AdminDocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository) {

        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Transactional
    public void deleteDocument(Long documentId) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Document not found"
                                ));

        documentChunkRepository.deleteByDocument(document);
        documentRepository.delete(document);
    }
}
