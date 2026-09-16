package com.university.knowledge.dto;

import com.university.knowledge.entity.Document;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String fileName,
        String title,
        String uploadedBy,
        LocalDateTime uploadedAt
) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getTitle(),
                document.getUploadedBy().getUsername(),
                document.getUploadedAt()
        );
    }
}
