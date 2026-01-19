# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FBA LogiAI is a cross-border e-commerce logistics AI Agent system built with DDD (Domain-Driven Design) + microservices architecture. It provides intelligent assistants for marketing, order booking, warehouse operations, and shipping management.

## Build Commands

```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl fba-logi-chat-service -am

# Skip tests
mvn clean install -DskipTests

# Run a specific service (from project root)
mvn spring-boot:run -pl fba-logi-chat-service
```

## Infrastructure Setup

```bash
# Start infrastructure (PostgreSQL, Redis, Milvus)
cd deploy && docker-compose -f docker-compose-infra.yml up -d

# Start observability stack (Prometheus, Grafana, Loki)
cd deploy && docker-compose -f docker-compose-observability.yml up -d

# Start application services
cd deploy && docker-compose -f docker-compose-services.yml up -d

# View logs
docker-compose -f docker-compose-infra.yml logs -f <service-name>

# Stop all
docker-compose -f docker-compose-infra.yml down
```

## Architecture

### Module Structure (DDD Layers)

- **fba-logi-common**: Shared utilities, constants, exceptions, response wrappers
- **fba-logi-api**: DTOs, request/response objects for external communication
- **fba-logi-domain**: Domain entities, repositories (interfaces), domain services - organized by bounded context (order, customer, marketing, shipping, warehouse, basedata)
- **fba-logi-infrastructure**: Repository implementations, database mappers (MyBatis-Plus), external adapters (LLM clients, Milvus, Kafka)
- **fba-logi-agent**: AI Agent framework - core execution, skills, workflows, sub-agents

### Microservices

- **fba-logi-gateway**: API Gateway (Spring Cloud Gateway)
- **fba-logi-chat-service**: Main chat service exposing Agent endpoints (port configurable)
- **fba-logi-data-service**: Data management service
- **fba-logi-rag-service**: RAG (Retrieval-Augmented Generation) service
- **fba-logi-writing-service**: Document analysis service

### Agent Framework (`fba-logi-agent`)

The Agent system follows a hierarchical pattern:

1. **OrchestratorAgent** (`subagent/OrchestratorAgent.java`): Main entry point that routes user requests to specialized sub-agents using `<delegate>` tags
2. **AgentExecutor** (`core/AgentExecutor.java`): Executes agents with multi-round tool calling support (max 5 rounds)
3. **Skills** (`skill/`): Atomic business operations that agents can invoke via `<tool_call>` tags
   - Implement `ISkill` interface
   - Organized by domain: `basedata/`, `customer/`, `marketing/`, `order/`, `shipping/`, `warehouse/`, `writing/`
4. **Workflows** (`workflow/`): Multi-step orchestration with node-based execution (AGENT, TOOL, CONDITION, CUSTOM nodes)

### LLM Integration

- **LlmClientFactory**: Creates LLM clients based on `LLM_PROVIDER` env var
- Supported providers: DeepSeek (OpenAI-compatible), Zhipu AI
- Vision support via `VisionLlmClientFactory` and `ZhipuVisionClient`

### Key Agent Types

- `marketing_strategist`: Marketing strategy agent
- `marketing_sales`: Sales assistant agent
- `order_booking`: Order booking agent
- `warehouse_ops`: Warehouse operations agent

## Tech Stack

- Java 21, Spring Boot 3.2.5, Spring Cloud 2023.0.1
- LangChain4j 0.36.2 for LLM integration
- MyBatis-Plus 3.5.7 with PostgreSQL
- Milvus 2.4.5 for vector storage
- Redis for caching
- Kafka for messaging

## Adding a New Skill

1. Create class in appropriate domain package under `fba-logi-agent/src/main/java/com/fba/logi/agent/skill/<domain>/`
2. Extend `AbstractSkill` or implement `ISkill`
3. Define `getSkillId()`, `getSkillName()`, `getDescription()`, `getParameterSchema()`
4. Implement `execute(SkillContext context, Map<String, Object> parameters)`
5. Register in `AgentSkillConfiguration` if not auto-scanned

## Environment Configuration

Copy `deploy/.env.example` to `deploy/.env` and configure:
- `DEEPSEEK_API_KEY` or `ZHIPU_API_KEY` for LLM
- `LLM_PROVIDER`: `deepseek` or `zhipu`
- Database credentials (PostgreSQL, Redis)
