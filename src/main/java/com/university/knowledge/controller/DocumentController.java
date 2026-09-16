package com.university.knowledge.controller;

import com.university.knowledge.dto.DocumentResponse;
import com.university.knowledge.entity.Document;
import com.university.knowledge.service.DocumentIngestionService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@Validated
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;

    public DocumentController(
            DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title")
            @NotBlank(message = "Title is required")
            @Size(
                    min = 2,
                    max = 200,
                    message = "Title must be between 2 and 200 characters"
            )
            String title,
            Authentication authentication)
            throws Exception {

        String username = authentication.getName();

        Document document =
                documentIngestionService.ingest(
                        file,
                        title,
                        username
                );

        return DocumentResponse.from(document);
    }
}
