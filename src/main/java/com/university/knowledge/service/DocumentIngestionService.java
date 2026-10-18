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
        DocumentEmbeddingService documentEmbeddingService,
        UserRepository userRepository) {


        this.pdfTextExtractorService = pdfTextExtractorService;
        this.textChunkingService = textChunkingService;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.documentEmbeddingService = documentEmbeddingService;
        this.userRepository = userRepository;
    }

    public Document ingest(
            MultipartFile file,
            String title,
            String uploadedBy) throws IOException {

        Path tempFile =
                Files.createTempFile("rag-upload-", ".pdf");

        try {
            file.transferTo(tempFile);

            String text =
                    pdfTextExtractorService.extractText(tempFile);

            List<String> chunks =
                    textChunkingService.chunk(text);
            User user=
                    userRepository.findByUsername(UploadedBy)        
                            .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                ));
           Document document =
                    documentRepository.save(
                            new Document(
                                    file.getOriginalFilename(),
                                    title,
                                    uploadedBy
                            )
                    );

            for (int i = 0; i < chunks.size(); i++) {

                DocumentChunk chunk =
                        new DocumentChunk(
                                document,
                                chunks.get(i),
                                i
                        );

                documentEmbeddingService
                        .generateAndStoreEmbedding(chunk);
            }

            return document;

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}