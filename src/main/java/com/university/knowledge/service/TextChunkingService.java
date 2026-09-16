package com.university.knowledge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

    private final int chunkSize;
    private final int overlap;

    public TextChunkingService(
            @Value("${rag.chunk-size:1000}") int chunkSize,
            @Value("${rag.chunk-overlap:200}") int overlap) {

        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be greater than 0");
        }

        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "Chunk overlap must be >= 0 and smaller than chunk size"
            );
        }

        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalizedText = text.trim();
        List<String> chunks = new ArrayList<>();

        int start = 0;

        while (start < normalizedText.length()) {
            int end = Math.min(start + chunkSize, normalizedText.length());

            String chunk = normalizedText.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end == normalizedText.length()) {
                break;
            }

            start = end - overlap;
        }

        return chunks;
    }
}
