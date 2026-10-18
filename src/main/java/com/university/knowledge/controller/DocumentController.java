package com.university.knowledge.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.university.knowledge.dto.DocumentResponse;
import com.university.knowledge.entity.Document;
import com.university.knowledge.service.DocumentIngestionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/documents")
@Validated
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;

    public DocumentController(
            DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    @Operation(
            summary = "Upload a university document",
            description = "Uploads a PDF document and ingests it into the RAG knowledge base.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    requiredProperties = {"file", "title"}
                            )
                    )
            )
    )
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