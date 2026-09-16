package com.university.knowledge.dto;

import jakarta.validation.constraints.NotBlank;

public record AskQuestionRequest(
        @NotBlank(message = "Question must not be blank")
        String question
) {
}
