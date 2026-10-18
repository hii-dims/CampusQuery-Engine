package com.university.knowledge.dto;

import java.util.List;

public record AskQuestionResponse(
        String answer,
        List<Source> sources
) {

    public record Source(
            Long documentId,
            String documentTitle,
            Integer chunkIndex
    ) {
    }
}