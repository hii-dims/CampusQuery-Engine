# CampusQuery-Engine 

A backend application built with **Spring Boot** and **Spring AI** that acts as a smart RAG (Retrieval-Augmented Generation) assistant for university data. Instead of digging through hundreds of pages of lecture notes and syllabi, this system lets students and faculty query institutional documents and get precise, grounded answers with citations.

---

##  Tech Stack & Tools

- **Backend:** Spring Boot, Spring AI, Java 17+
- **Database & Vector Store:** PostgreSQL with the `pgvector` extension
- **AI & Embeddings:** OpenAI Embedding API / Ollama (for local models like Nomic and Qwen)
- **Performance:** Spring Boot Caching (`@Cacheable`)
- **Build Tool:** Maven

---

##  What I Built & Key Features

- **Metadata-Aware Ingestion:** When PDFs (syllabi, notes) are uploaded, they aren't just split blindly. They are tagged with academic metadata like `courseCode`, `semester`, and `department`, allowing searches to target specific classes.
- **Smart Embedding Caching:** Generating embeddings repeatedly can be slow and expensive. I added Spring caching (`@Cacheable`) so identical text lookups load instantly from memory.
- **Vector Similarity Search:** Uses PostgreSQL and `pgvector` to perform fast cosine similarity searches across document chunks.
- **Local & Cost-Effective Inference:** Integrated with local Ollama models alongside REST clients, keeping local testing fast and free.

---

##  Project Architecture

[PDF / Document Upload]
│
▼
[Token Chunking & Metadata Tagging]
│
▼
[Embedding Generation (Cached via Spring)]
│
▼
[PostgreSQL + PGVector (Vector Storage & Filtering)]
│
▼
[RAG Prompt Builder & Local LLM (Ollama) ➔ Answer + Citations]

---

##  Getting Started Locally

### Prerequisites
- Java 17 or higher installed on your machine
- PostgreSQL installed with the `pgvector` extension enabled
- Ollama running locally (for local model inference)

### 1. Clone the Repo
```bash
git clone [https://github.com/hii-dims/CampusQuery-Engine.git](https://github.com/hii-dims/CampusQuery-Engine.git)
cd CampusQuery-Engine