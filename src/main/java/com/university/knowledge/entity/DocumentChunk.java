package com.university.knowledge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer chunkIndex;

    protected DocumentChunk() {
    }

    public DocumentChunk(
            Document document,
            String content,
            Integer chunkIndex) {

        this.document = document;
        this.content = content;
        this.chunkIndex = chunkIndex;
    }

    public Long getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public String getContent() {
        return content;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }
}