# Spring AI Learning Plan: Building "Archie" — An AI Research Assistant

## Context

Starting from an empty Java project, this plan builds a fully agentic AI research assistant using **Spring AI** and **Java**. The project ("Archie") evolves each week, adding new capabilities in layers — from simple chat to RAG-powered knowledge retrieval to autonomous multi-agent orchestration. The pacing is ~1 hour/day over 3 weeks.

**Stack:** Spring Boot 3.x · Spring AI 1.0 · Claude claude-sonnet-4-6 (main) · claude-haiku-4-5-20251001 (lightweight) · OpenAI embeddings · PGVector (Docker) · Maven

---

## The Project: "Archie"

A single project that grows across all three weeks:

| Week | Theme | Archie can… |
|------|-------|-------------|
| 1 | Foundations | Talk, remember, stream, return structured data |
| 2 | Knowledge (RAG) | Read PDFs, retrieve facts, cite sources |
| 3 | Agency | Use tools, call MCP servers, orchestrate sub-agents |

---

## Setup (Before Week 1 — ~30 min)

- Generate project at [start.spring.io](https://start.spring.io): Spring Boot 3.x, Java 21, Maven
- Add to `pom.xml`:
  - `spring-ai-bom` (v1.0.0) in `<dependencyManagement>`
  - `spring-ai-anthropic-spring-boot-starter`
  - `spring-ai-openai-spring-boot-starter` (for embeddings)
  - `spring-ai-pgvector-store-spring-boot-starter`
  - `spring-ai-pdf-document-reader`
  - `spring-ai-mcp-server-spring-boot-starter`
  - `spring-ai-mcp-client-spring-boot-starter`
  - `spring-boot-starter-web`
- Configure `application.yml` with `ANTHROPIC_API_KEY` and `OPENAI_API_KEY`
- Start Postgres with pgvector via Docker: `docker run -e POSTGRES_PASSWORD=pass -p 5432:5432 pgvector/pgvector:pg16`

---

## Week 1 — Foundations: Teaching Archie to Speak

**Goal:** A live, stateful, streaming conversational assistant backed by Claude.

### Day 1 — First Chat Completion
- Learn: What is a chat completion? Stateless model, stateful app mental model.
- Build: `GET /ask?question=` endpoint using `ChatClient` → returns Claude's text response.
- Key classes: `ChatClient`, `ChatClient.Builder`

### Day 2 — Prompt Templates & System Prompts
- Learn: Prompts are parameterized programs, not hardcoded strings.
- Build: Move prompts to `.st` resource files. Create a `ResearchAssistantPrompt.st` with `{topic}` and `{userQuestion}` variables. Wire two endpoints: `/ask/general` and `/ask/technical`, each with a different system prompt.
- Key classes: `PromptTemplate`, `SystemPromptTemplate`, `UserMessage`, `SystemMessage`

### Day 3 — Structured Output
- Learn: How to bridge LLM text output with Java type system.
- Build: Define a `ResearchSummary` record (topic, keyFindings, confidenceLevel enum, followUpQuestions). Build `GET /research/summary?topic=` that returns a populated Java object, not a raw string.
- Key classes: `BeanOutputConverter`, format instructions injection

### Day 4 — Chat Memory
- Learn: The Advisor pattern. How Spring AI decorates `ChatClient` with cross-cutting concerns.
- Build: Add `conversationId` parameter to `/chat`. Use `InMemoryChatMemory` + `MessageChatMemoryAdvisor`. Test multi-turn conversation context.
- Key classes: `ChatMemory`, `InMemoryChatMemory`, `MessageChatMemoryAdvisor`

### Day 5 — Streaming + Week 1 Milestone
- Learn: Token streaming via Project Reactor. Why it matters for UX.
- Build: `GET /chat/stream` returning SSE via `Flux<String>`. Test with `curl --no-buffer`.
- **Milestone:** Archie is a live, stateful, streaming assistant with a defined personality, structured output capability, and per-session memory.

---

## Week 2 — Knowledge: Teaching Archie to Read

**Goal:** A RAG-powered assistant that answers questions from your documents.

### Day 6 — Embeddings: Meaning as Math
- Learn: What are vector embeddings? Cosine similarity. Why semantic search works.
- Build: Wire `EmbeddingClient` (OpenAI `text-embedding-ada-002`). Build `GET /similarity?a=&b=` that returns a 0–1 similarity score between two phrases. Observe that related phrases score high even without shared words.
- Key classes: `EmbeddingClient`, `EmbeddingResponse`

### Day 7 — PGVector Setup & Document Ingestion
- Learn: Vector stores. Difference from relational DBs. Why pgvector for production.
- Build: Configure `PgVectorStore` Spring bean. Write `DocumentIngestionService` that wraps text as `Document` objects with metadata, embeds them, and stores them. Build `GET /search?query=` that does semantic similarity search.
- Key classes: `VectorStore`, `PgVectorStore`, `Document`, `VectorStore.similaritySearch()`
- **Note:** Verify Docker Postgres is running before this day.

### Day 8 — PDF Loading & Chunking
- Learn: Ingestion pipelines. Why chunking matters. Token-aware splitting vs. character splitting.
- Build: Load 2–3 public research PDFs (any topic you like). Build `PdfIngestionPipeline` using `PagePdfDocumentReader` + `TokenTextSplitter` with overlapping chunks. Store chunks with metadata (source file, page number). Add `POST /ingest` endpoint.
- Key classes: `PagePdfDocumentReader`, `TokenTextSplitter`, chunk size + overlap configuration

### Day 9 — RAG in Action
- Learn: Retrieval Augmented Generation. How retrieved context prevents hallucinations.
- Build: Add `QuestionAnswerAdvisor` to `ChatClient`. Create `GET /chat/grounded` that answers questions using only retrieved document context. Test with questions answerable only from your PDFs. Add metadata filter to scope questions to specific source files.
- Key classes: `QuestionAnswerAdvisor`, `SearchRequest`, `SimilarityThreshold`

### Day 10 — Conversation Memory in Vector Store + Week 2 Milestone
- Learn: Layered memory: short-term (in-memory) vs. semantic (vector store). LLM-as-summarizer pattern.
- Build: After every 5 exchanges, summarize the conversation using claude-haiku-4-5-20251001 and store the summary as a `Document` in PGVector. Build a custom `UserMemoryAdvisor` that retrieves relevant past summaries at conversation start.
- **Milestone:** Archie knows everything in your PDFs, cites sources, and remembers what you researched in past sessions.

---

## Week 3 — Agency: Teaching Archie to Act

**Goal:** An autonomous multi-agent system using tools and MCP.

### Day 11 — Tool Calling: Giving Archie Hands
- Learn: How the model acts as orchestrator. The tool-call lifecycle (model requests → app executes → result returned).
- Build: Create `ResearchTools` with three `@Tool` methods: `searchWikipedia(topic)` (real Wikipedia API), `getCurrentDateTime()`, `saveResearchNote(note)`. Register tools with `ChatClient`. Ask Archie to research a topic and save a summary — watch it autonomously chain tool calls.
- Key classes: `@Tool`, `ToolCallback`, tool registration on `ChatClient`

### Day 12 — Agentic Loops & Multi-Step Tool Use
- Learn: What is an agentic loop? Plan → Act → Observe → Replan. How Spring AI handles multi-step tool calling.
- Build: Add a mock `WebSearchTool` (returns hardcoded realistic results for a few topics) and a `DocumentWriterTool` (formats a Markdown report). Build `GET /research/deep?topic=` with a system prompt instructing Archie to produce a full report using all tools autonomously. Add error handling: define what happens when a tool throws.
- Key classes: multi-step tool calling, `ToolCallResultConverter`, agentic loop termination

### Day 13 — MCP: The USB Standard for AI Tools
- Learn: What is MCP (Model Context Protocol)? Why it matters for tool ecosystems. Server vs. client roles.
- Build: Expose `ResearchTools` as an MCP server using `spring-ai-mcp-server-spring-boot-starter`. Build a second `@Configuration` in the same app acting as MCP client. Wire `McpClient` into a new `ChatClient` that discovers tools dynamically via MCP without direct Java dependencies on the tool implementations.
- Key classes: `McpServer`, `McpClient`, `McpTransport`, dynamic tool discovery
- Reference: [Spring AI MCP Docs](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html) · [MCP Spec](https://modelcontextprotocol.io/introduction)

### Day 14 — Multi-Agent Orchestration
- Learn: Agent specialization. Orchestrator pattern. How to chain agent outputs as inputs.
- Build: Three specialized agents as Spring beans (distinct system prompts + tool sets):
  1. `ResearcherAgent` — gathers raw facts using Wikipedia + web search tools
  2. `AnalystAgent` — extracts insights from raw facts (reasoning only, no tools)
  3. `WriterAgent` — produces polished Markdown reports from analyst output

  Build `OrchestratorService` that chains them: Researcher → Analyst → Writer. Expose as `GET /research/full?topic=`.
- Key concepts: prompt chaining, inter-agent context passing, agent specialization via system prompts

### Day 15 — Observability, Evaluation + Final Milestone
- Learn: LLM-as-judge evaluation. Token usage tracking. Why observability is non-negotiable in production.
- Build:
  - Enable Spring AI observability (Micrometer + Actuator). Log token usage and latency per call.
  - Build `EvaluationService` that uses claude-haiku-4-5-20251001 to score Archie's answers (1–5) for accuracy, completeness, and groundedness against manual ground-truth answers.
  - Run the full pipeline on 5 test questions. Review scores. Identify weak points.
- Key classes: `ChatObservation`, `ObservationRegistry`, LLM-as-judge pattern
- **Final Milestone:** Archie is a fully autonomous, observable, multi-agent research assistant using MCP for tool discovery, RAG for grounded knowledge, and specialized sub-agents for complex tasks.

---

## Key Resources

| Resource | When to Read |
|----------|-------------|
| [Spring AI Reference Docs](https://docs.spring.io/spring-ai/reference/) | Throughout — bookmark Chat Client, Advisors, Tools sections |
| [Spring AI GitHub Examples](https://github.com/spring-projects/spring-ai) | Week 1–2 |
| [Anthropic API Docs — Tool Use](https://docs.anthropic.com/en/docs/tool-use) | Before Day 11 |
| [MCP Specification](https://modelcontextprotocol.io/introduction) | Before Day 13 |
| [Spring AI MCP Docs](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html) | Day 13 |

---

## Unresolved Questions

- Docker already installed, or does Postgres setup need a Docker install step on Day 7?
- OpenAI API key available, or does embedding provider need to change?
- Preferred IDE (IntelliJ / VS Code)? Affects any tooling-specific tips.
