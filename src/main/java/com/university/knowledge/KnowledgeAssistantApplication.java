package com.university.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class KnowledgeAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnowledgeAssistantApplication.class, args);
	}
}