package com.university.knowledge.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.knowledge.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}