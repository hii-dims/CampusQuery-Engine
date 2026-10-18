package com.university.knowledge.service;

import com.university.knowledge.entity.Document;
import com.university.knowledge.entity.DocumentChunk;
import com.university.knowledge.repository.DocumentChunkRepository;
import com.university.knowledge.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import com.university.knowledge.entity.User;
import com.university.knowledge.repository.UserRepository;

@Service
public class DocumentIngestionService {

    private final PdfTextExtractorService pdfTextExtractorService;
    private final TextChunkingService textChunkingService;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentEmbeddingService documentEmbeddingService;
    private final UserRepository userRepository;

    public DocumentIngestionService(
            PdfTextExtractorService pdfTextExtractorService,
            TextChunkingService textChunkingService,
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository, // Fixed: Added missing dependency injection
            DocumentEmbeddingService documentEmbeddingService,
            UserRepository userRepository) {

        this.pdfTextExtractorService = pdfTextExtractorService;
        this.textChunkingService = textChunkingService;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository; // Fixed: Assigned properly
        this.documentEmbeddingService = documentEmbeddingService;
        this.userRepository = userRepository;
    }

    /**
     * Upgraded ingest method accepting academic metadata for enterprise RAG.
     */
    public Document ingest(
            MultipartFile file,
            String title,
            String uploadedBy,
            String courseCode,   // Added academic context
            String semester,     // Added academic context
            String department) throws IOException { // Added academic context

        Path tempFile = Files.createTempFile("rag-upload-", ".pdf");

        try {
            file.transferTo(tempFile);

            String text = pdfTextExtractorService.extractText(tempFile);

            List<String> chunks = textChunkingService.chunk(text);

            // Fixed typo: UploadedBy -> uploadedBy
            User user = userRepository.findByUsername(uploadedBy)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Document document = documentRepository.save(
                    new Document(
                            file.getOriginalFilename(),
                            title,
                            uploadedBy
                    )
            );

            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = new DocumentChunk(
                        document,
                        chunks.get(i),
                        i
                );

                // Save chunk to repository before embedding generation
                documentChunkRepository.save(chunk);

                documentEmbeddingService
                        .generateAndStoreEmbedding(chunk);
            }

            return document;

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}