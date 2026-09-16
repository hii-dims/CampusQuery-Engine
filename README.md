# University Knowledge Assistant

A Java-based Retrieval-Augmented Generation (RAG) backend that allows users to upload university PDF documents and ask questions about their contents.

The application extracts text from PDFs, splits the text into chunks, generates vector embeddings locally using Ollama, stores those embeddings in PostgreSQL with pgvector, retrieves relevant chunks for a question, and uses a local LLM to generate an answer with source citations.

## Tech Stack

* Java 17
* Spring Boot 4.1.1
* Spring Web MVC
* Spring Data JPA
* Spring Security
* PostgreSQL
* pgvector
* Apache PDFBox 3.0.6
* Ollama
* Qwen 3 4B Instruct
* nomic-embed-text
* Maven
* Docker Compose
* Swagger / OpenAPI
* JUnit

## Architecture

The application follows this RAG pipeline:

```text
                    ┌─────────────────────┐
                    │     PDF Upload      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ PDF Text Extraction │
                    │      PDFBox         │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Text Chunking    │
                    │ 1000 chars / 200 ov.│
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │  Ollama Embeddings  │
                    │  nomic-embed-text   │
                    │    768 dimensions   │
                    └──────────┬──────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │ PostgreSQL + pgvector          │
              │ Documents + Chunks + Vectors   │
              └────────────────┬───────────────┘
                               │
                         User Question
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Question Embedding  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ pgvector Similarity │
                    │       Search        │
                    └──────────┬──────────┘
                               │
                         Top 5 Chunks
                               │
                               ▼
                    ┌─────────────────────┐
                    │     Qwen LLM        │
                    │  qwen3:4b-instruct  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Answer + Citations  │
                    └─────────────────────┘
```

## How RAG Works

RAG stands for **Retrieval-Augmented Generation**.

Instead of asking the LLM to answer directly from its general knowledge, the application first retrieves relevant information from the uploaded university documents.

The retrieved document chunks are then supplied to the LLM as context.

The process is:

1. A user uploads a PDF.
2. PDFBox extracts the text.
3. The text is divided into overlapping chunks.
4. Each chunk is converted into a 768-dimensional vector embedding.
5. The embeddings are stored in PostgreSQL using pgvector.
6. A user asks a question.
7. The question is converted into an embedding.
8. pgvector finds the most similar document chunks.
9. The top five chunks are provided to the Qwen LLM.
10. The LLM generates an answer using the retrieved context.
11. The API returns the answer together with source information.

## Main Features

* PDF document ingestion
* PDF text extraction
* Configurable text chunking
* Overlapping chunks for better context preservation
* Local vector embedding generation
* 768-dimensional embeddings
* PostgreSQL persistence
* pgvector similarity search
* Retrieval-Augmented Generation
* Local Qwen LLM
* Source citations
* Conversation history
* User registration
* BCrypt password hashing
* HTTP Basic authentication
* Role-based authorization
* Admin-only document deletion
* Bean Validation
* Centralized API error handling
* Swagger / OpenAPI documentation
* Automated tests
* Docker Compose PostgreSQL setup

## Document Ingestion Pipeline

When an authenticated user uploads a PDF, the backend performs the following operations:

```text
Multipart PDF
     ↓
DocumentIngestionService
     ↓
PdfTextExtractorService
     ↓
TextChunkingService
     ↓
DocumentEmbeddingService
     ↓
Ollama / nomic-embed-text
     ↓
PostgreSQL + pgvector
```

The document metadata is stored in the `documents` table.

Individual text chunks are stored in `document_chunks`.

Each chunk can have a 768-dimensional vector embedding associated with it.

## Question Answering Pipeline

When a user asks a question:

```text
User Question
     ↓
RagService
     ↓
SimilaritySearchService
     ↓
Question Embedding
     ↓
pgvector Similarity Search
     ↓
Top 5 Relevant Chunks
     ↓
Qwen LLM
     ↓
Generated Answer
     ↓
Source Citations
```

Conversation messages are also persisted so that conversation history can be maintained.

## API Endpoints

### Register User

```http
POST /api/auth/register
```

Creates a new user account.

Example request:

```json
{
  "username": "student123",
  "password": "password123"
}
```

Passwords are encoded with BCrypt before being stored.

### Upload Document

```http
POST /api/documents/upload
```

Requires authentication.

Multipart fields:

* `file` — PDF document
* `title` — document title

The document is processed through the complete ingestion pipeline.

### Ask a Question

```http
POST /api/rag/ask
```

Requires authentication.

The endpoint performs semantic retrieval and generates an answer using the retrieved document context.

The response contains:

* Generated answer
* Document ID
* Document title
* File name
* Chunk index

### Delete Document

```http
DELETE /api/admin/documents/{id}
```

Requires the `ADMIN` role.

The document's chunks are deleted before the document itself inside a transaction to respect the database foreign-key relationship.

### Health Check

```http
GET /api/health
```

Returns application health information.

## Security

Spring Security protects the application's API endpoints.

Authorization rules include:

* User registration is publicly accessible.
* Health and Swagger endpoints are publicly accessible.
* Application APIs require authentication.
* `/api/admin/**` requires the `ADMIN` role.
* Passwords are stored using BCrypt hashing.
* API responses use DTOs instead of exposing JPA entities directly.
* Environment secrets are stored in `.env`.
* `.env` is excluded from Git using `.gitignore`.

## Authentication Flow

```text
HTTP Basic Credentials
        ↓
Spring Security
        ↓
UserDetailsService
        ↓
UserRepository
        ↓
PostgreSQL
        ↓
BCrypt Password Verification
        ↓
Authenticated User
```

The authenticated username is obtained from Spring Security rather than trusting a username supplied by the client for document ownership.

## Database Model

The main entities are:

```text
User
 │
 └── Document
      │
      └── DocumentChunk
           └── embedding vector(768)

ConversationMessage
```

### User

Stores:

* ID
* Username
* BCrypt password hash
* Role

### Document

Stores:

* ID
* File name
* Title
* Uploading user
* Upload timestamp

### DocumentChunk

Stores:

* ID
* Parent document
* Chunk content
* Chunk index
* Vector embedding

### ConversationMessage

Stores conversation history associated with users/conversations.

## PostgreSQL + pgvector

PostgreSQL is used as the main relational database.

The pgvector extension allows vector embeddings to be stored and searched directly inside PostgreSQL.

The application uses vector similarity search to find chunks that are semantically related to the user's question.

This avoids introducing a separate vector database for the project.

## Chunking Configuration

The application uses configurable chunking settings:

```properties
rag.chunk-size=1000
rag.chunk-overlap=200
```

A chunk size of 1000 characters is used with 200 characters of overlap.

The overlap helps preserve context between neighboring chunks.

## Local AI Models

The project uses Ollama for local AI inference.

### Embedding Model

```text
nomic-embed-text
```

The embedding model produces:

```text
768-dimensional vectors
```

### Generation Model

```text
qwen3:4b-instruct
```

The local Qwen model generates the final answer using the retrieved document context.

Using local models allows the core RAG pipeline to run locally.

## Swagger / OpenAPI

Swagger UI is available when the application is running:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger makes it easier to inspect and test the REST API during development.

## Running Locally

### Prerequisites

Install:

* Java 17
* Docker
* Docker Compose
* Ollama

### 1. Start PostgreSQL

From the project root:

```bash
docker compose up -d
```

The PostgreSQL container uses the pgvector-enabled image.

Check the container:

```bash
docker ps
```

### 2. Start Ollama Models

Pull the required models:

```bash
ollama pull nomic-embed-text
ollama pull qwen3:4b-instruct
```

Make sure Ollama is running before testing document ingestion or RAG.

### 3. Configure Environment Variables

Create a `.env` file in the project root:

```text
OPENAI_API_KEY=your_key_here
```

The `.env` file is intentionally excluded from Git.

### 4. Start the Spring Boot Application

Use Java 17:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

### 5. Run Tests

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw test
```

## Docker

Docker Compose is used for PostgreSQL.

The database configuration is:

```text
Database: university_knowledge
Username: rag_user
Port: 5432
```

Start the database:

```bash
docker compose up -d
```

Stop the database:

```bash
docker compose down
```

## Project Structure

```text
university-knowledge-assistant/
│
├── .gitignore
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
│
├── src/
│   ├── main/
│   │   ├── java/com/university/knowledge/
│   │   │
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   │
│   │   ├── controller/
│   │   │   ├── AdminDocumentController.java
│   │   │   ├── AuthController.java
│   │   │   ├── DocumentController.java
│   │   │   ├── HealthController.java
│   │   │   └── RagController.java
│   │   │
│   │   ├── dto/
│   │   │   ├── ApiErrorResponse.java
│   │   │   ├── AskQuestionRequest.java
│   │   │   ├── AskQuestionResponse.java
│   │   │   ├── DocumentResponse.java
│   │   │   ├── RegisterRequest.java
│   │   │   └── UserResponse.java
│   │   │
│   │   ├── entity/
│   │   │   ├── ConversationMessage.java
│   │   │   ├── Document.java
│   │   │   ├── DocumentChunk.java
│   │   │   └── User.java
│   │   │
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java
│   │   │
│   │   ├── repository/
│   │   │   ├── ConversationMessageRepository.java
│   │   │   ├── DocumentChunkRepository.java
│   │   │   ├── DocumentRepository.java
│   │   │   └── UserRepository.java
│   │   │
│   │   └── service/
│   │       ├── AdminDocumentService.java
│   │       ├── AuthService.java
│   │       ├── DocumentEmbeddingService.java
│   │       ├── DocumentIngestionService.java
│   │       ├── OpenAiEmbeddingService.java
│   │       ├── PdfTextExtractorService.java
│   │       ├── RagService.java
│   │       ├── SimilaritySearchService.java
│   │       └── TextChunkingService.java
│   │
│   └── test/
│       └── java/com/university/knowledge/
│           ├── KnowledgeAssistantApplicationTests.java
│           └── RegisterRequestValidationTest.java
│
└── .env
   └── Not committed to Git
```

## Error Handling

The application uses Bean Validation for validating incoming requests.

Validation errors are handled centrally using `@RestControllerAdvice`.

Example validation response:

```json
{
  "timestamp": "2026-09-16T12:00:00",
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "username": "Username is required"
  }
}
```

This provides a consistent error format across the API.

## Testing

The project includes automated tests for:

* Spring application context startup
* Valid registration requests
* Blank username validation
* Short password validation

Run all tests with:

```bash
./mvnw test
```

The complete Maven test suite currently passes successfully.

## Key Design Decisions

### Local AI Inference

Ollama is used for local embeddings and LLM generation, allowing the RAG pipeline to run locally.

### PostgreSQL + pgvector

Vector storage and similarity search are handled inside PostgreSQL rather than introducing a separate vector database.

### DTOs

DTOs separate API contracts from database entities and prevent sensitive fields such as password hashes from being exposed.

### BCrypt

Passwords are never stored as plaintext. BCrypt is used to create password hashes that can be verified during authentication.

### Role-Based Authorization

Spring Security separates normal authenticated users from administrative operations.

### Transactional Deletion

Document chunks are deleted before the parent document within a transaction to maintain referential integrity.

### Conversation History

Conversation messages are persisted so that previous interactions can be used as part of the application's conversational functionality.

## Future Improvements

Potential improvements include:

* JWT-based authentication
* Refresh tokens
* Document listing and search
* Streaming LLM responses
* RAG evaluation metrics
* Retrieval re-ranking
* Hybrid keyword + vector search
* More integration tests
* Production-ready secret management
* Observability and metrics
* CI/CD pipeline
* Better document metadata management

