package com.university.knowledge.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.university.knowledge.dto.AskQuestionRequest;
import com.university.knowledge.dto.AskQuestionResponse;
import com.university.knowledge.service.RagService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")
    public AskQuestionResponse ask(
            @Valid @RequestBody AskQuestionRequest request) {

        return ragService.ask(request.question());
    }
}