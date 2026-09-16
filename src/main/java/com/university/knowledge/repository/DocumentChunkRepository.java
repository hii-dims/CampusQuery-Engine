package com.university.knowledge.repository;

import com.university.knowledge.entity.Document;
import com.university.knowledge.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

    @Modifying
    @Query(value = """
            UPDATE document_chunks
            SET embedding = CAST(:embedding AS vector)
            WHERE id = :id
            """, nativeQuery = true)
    void updateEmbedding(
            @Param("id") Long id,
            @Param("embedding") String embedding
    );

    @Query(value = """
            SELECT *
            FROM document_chunks
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );

    void deleteByDocument(Document document);
}
