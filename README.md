# University Knowledge Assistant

A Java-based Retrieval-Augmented Generation (RAG) backend that allows users to upload university PDF documents and ask questions about their contents.

The application extracts text from PDFs, splits the text into chunks, generates vector embeddings locally using Ollama, stores those embeddings in PostgreSQL with pgvector, retrieves relevant chunks for a question, and uses a local LLM to generate an answer with source citations.

## Problem

University documents often contain information spread across multiple PDF files, making it difficult to quickly find specific policies, requirements, and procedures. This project provides a local RAG-based assistant that lets users upload those documents and ask natural-language questions while grounding responses in the uploaded content.


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

## How the RAG Pipeline Works

RAG (Retrieval-Augmented Generation) combines information retrieval with language generation.

For this project, the LLM does not answer questions from its general knowledge alone. Instead, the application first searches the uploaded university documents for relevant content and then provides the retrieved passages to the LLM as context.

### 1. Document Ingestion

When a PDF is uploaded:

```text
PDF
 ↓
Text Extraction
 ↓
Text Chunking
 ↓
Embedding Generation
 ↓
PostgreSQL + pgvector
```

- Apache PDFBox extracts text from the PDF.
- The extracted text is split into overlapping chunks.
- Each chunk is converted into a vector embedding using Ollama and `nomic-embed-text`.
- The document, chunks, and embeddings are stored in PostgreSQL using pgvector.

### 2. Question Answering

When a user asks a question:

```text
Question
 ↓
Question Embedding
 ↓
Vector Similarity Search
 ↓
Top 5 Relevant Chunks
 ↓
Qwen 3 4B Instruct
 ↓
Answer + Sources
```

The question is converted into an embedding and compared with stored document embeddings.

The five most relevant chunks are retrieved and passed to the local Qwen model as context. The generated response is then returned together with source information from the retrieved documents.

This approach helps keep answers grounded in the content of the uploaded documents rather than relying entirely on the model's pretrained knowledge.


## API Endpoints

| Method | Endpoint | Authentication | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user account |
| `POST` | `/api/documents/upload` | Authenticated | Upload and process a PDF document |
| `POST` | `/api/rag/ask` | Authenticated | Retrieve relevant chunks and generate an answer |
| `DELETE` | `/api/admin/documents/{id}` | `ADMIN` role | Delete a document and its associated chunks |
| `GET` | `/api/health` | Public | Check application health |

### Example: Register User

```json
{
  "username": "student123",
  "password": "password123"
}
```

### Example: Ask a Question

```json
{
  "question": "What is the attendance requirement for students?"
}
```

The RAG response includes the generated answer together with source information such as document ID, document title, file name, and chunk index.

## Security

The application uses Spring Security with HTTP Basic authentication and role-based authorization.

- Public access for user registration, health checks, and Swagger/OpenAPI documentation
- Authentication required for document upload and RAG question answering
- `ADMIN` role required for document deletion
- Passwords hashed using BCrypt
- DTOs used to avoid exposing persistence entities directly
- Secrets and environment-specific configuration kept outside the repository

## Authentication Flow

The application uses HTTP Basic authentication for protected API requests.

```text
Register
   ↓
Username + Password
   ↓
BCrypt Password Hashing
   ↓
PostgreSQL
   ↓
HTTP Basic Authentication
   ↓
Spring Security
   ↓
Role-Based Authorization
   ↓
Protected API Endpoint
```

Users authenticate with their registered credentials. Spring Security validates the credentials and applies role-based access rules to protected endpoints.

## Database Model

The application uses a relational data model to manage users, documents, document chunks, embeddings, and conversation history.

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

Stores user identity and authentication data, including username, BCrypt password hash, and role.

### Document

Stores document metadata such as file name, title, uploading user, and upload timestamp.

### DocumentChunk

Stores individual text chunks extracted from documents, including chunk content, chunk index, and the associated 768-dimensional vector embedding.

### ConversationMessage

Stores conversation history associated with users and their interactions with the RAG assistant.

## PostgreSQL + pgvector

PostgreSQL is used as the primary relational database and vector store.

The pgvector extension stores 768-dimensional embeddings alongside the application's relational data and enables vector similarity search.

When a user asks a question, the application compares the question embedding with stored document chunk embeddings to retrieve semantically relevant content.

Using PostgreSQL + pgvector keeps the project architecture simple by avoiding a separate vector database.


## Chunking Configuration

The application uses configurable text chunking settings:

```properties
rag.chunk-size=1000
rag.chunk-overlap=200
```

Each document is split into chunks of up to 1000 characters with 200 characters of overlap. The overlap helps preserve context across chunk boundaries during retrieval.

## Local AI Models

The project uses [Ollama](https://ollama.com/) to run both embedding generation and local LLM inference.

### Embedding Model

`nomic-embed-text` generates 768-dimensional vector embeddings for document chunks and user questions.

### Generation Model

`qwen3:4b-instruct` generates answers using the document chunks retrieved by the RAG pipeline.

Using local models keeps the core AI pipeline self-contained and avoids requiring a hosted LLM API for inference.


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

Install the following:

- Java 17
- Docker
- Docker Compose
- Ollama

### 1. Start PostgreSQL

From the project root:

```bash
docker compose up -d
```

Verify the container is running:

```bash
docker ps
```

### 2. Start Ollama

Make sure Ollama is running, then pull the required models:

```bash
ollama pull nomic-embed-text
ollama pull qwen3:4b-instruct
```

### 3. Start the Spring Boot Application

Use Java 17:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw spring-boot:run
```

The application runs on `http://localhost:8080`.

### 4. Run Tests

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw test
```


## Docker

Docker Compose provides the PostgreSQL database used by the application. The configured database uses a pgvector-enabled PostgreSQL image.

### Database Configuration

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
```



## Error Handling

The application uses Bean Validation for validating incoming API requests.

Validation errors are handled centrally using `@RestControllerAdvice`, providing a consistent error response format across the API.

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

## Testing

The project includes automated tests covering:

- Spring application context startup
- Valid registration requests
- Blank username validation
- Short password validation

Run the test suite with:

```bash
./mvnw test
```
## Key Design Decisions

### Local AI Inference

Ollama is used for local embeddings and LLM generation, allowing the RAG pipeline to run locally without relying on a hosted inference API.

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

Conversation messages are persisted to support the application's conversational functionality.


## What I Learned

Building this project gave me hands-on experience with:

- Designing a RAG pipeline from document ingestion through retrieval and generation
- Generating and storing vector embeddings with Ollama and pgvector
- Implementing semantic similarity search in PostgreSQL
- Building layered REST APIs with Spring Boot
- Applying Spring Security, HTTP Basic authentication, BCrypt, and role-based authorization
- Designing relational entities for documents, chunks, users, and conversation history
- Handling validation and API errors consistently
- Running AI services locally with Ollama
- Using Docker Compose to manage the PostgreSQL development environment

## Example Queries

After uploading a university PDF, users can ask questions such as:

- `What is the attendance requirement for students?`
- `What are the eligibility requirements for this course?`
- `What documents are required for registration?`
- `What is the examination policy?`

Example request:

```json
{
  "question": "What is the attendance requirement for students?"
}
```

The response contains the generated answer together with source information from the retrieved document chunks.


## Demo / Screenshots

The application can be explored through its REST APIs using Swagger / OpenAPI.

Recommended screenshots for the project README:

- Swagger UI showing the available API endpoints
- PDF document upload request and response
- RAG question request and generated answer with source information
- PostgreSQL / pgvector data after document ingestion

Screenshots can be added to the repository later under a `docs/images/` directory.


## Future Improvements

Potential improvements include:

- JWT-based authentication
- Refresh tokens
- Document listing and search
- Streaming LLM responses
- RAG evaluation metrics
- Retrieval re-ranking
- Hybrid keyword + vector search
- More integration tests
- Production-ready secret management
- Observability and metrics
- CI/CD pipeline
- Better document metadata management

