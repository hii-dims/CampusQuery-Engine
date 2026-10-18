package com.university.knowledge.controller;

import com.university.knowledge.service.AdminDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/documents")
public class AdminDocumentController {

    private final AdminDocumentService adminDocumentService;

    public AdminDocumentController(
            AdminDocumentService adminDocumentService) {

        this.adminDocumentService = adminDocumentService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id) {

        adminDocumentService.deleteDocument(id);

        return ResponseEntity.noContent().build();
    }
}
