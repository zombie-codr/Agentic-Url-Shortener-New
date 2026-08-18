# Agentic Software Engineering System — URL Shortener

This repository demonstrates controlled agent autonomy across the SDLC. The URL shortener is the product being engineered; the orchestrator is the control plane that interprets requirements, decomposes work, executes agents against an explicit dependency graph, enforces approval/policy gates, tracks decision context, performs bounded retry, and safely stops when recovery limits are exhausted.

## Modules
- `url-shortener`: runnable Spring Boot URL service on port 8080.
- `orchestrator`: agentic SDLC orchestration service on port 8081.
- `docs/scenarios`: greenfield, brownfield and ambiguous scenario narratives.

## URL Shortener APIs
- `POST /api/v1/urls` — create a short URL.
- `GET /{shortCode}` — redirect; returns `410 Gone` when expired.
- `GET /api/v1/urls/{shortCode}/analytics` — aggregate click analytics.

Example:
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com","customAlias":"demo123"}'

curl -i http://localhost:8080/demo123
curl http://localhost:8080/api/v1/urls/demo123/analytics
```

## Orchestration Model
A `WorkflowExecution` owns state and an `EngineeringContext`. Each `WorkflowNode` declares dependencies, responsible agent, retry policy and whether human approval is required. `WorkflowEngine` schedules only nodes whose dependencies have succeeded. Outputs are validated by `PolicyEngine` before becoming shared context. Approval nodes pause execution. Failed nodes retry only within a bounded budget; exhaustion results in `SAFE_STOPPED`.

### Governance principles
1. Agents reason and produce proposals; orchestration owns lifecycle transitions.
2. High-impact architecture, privacy and release decisions require human approval.
3. Cross-stage context and decision lineage live outside LLM chat history.
4. Failure recovery is bounded and observable.
5. Security and compliance policies can block outputs before downstream execution.
6. Upstream changes should invalidate/re-plan only affected downstream graph nodes; this is the next implementation increment.

## Running
Requirements: JDK 21 and Maven 3.9+.

```bash
mvn test
mvn -pl url-shortener spring-boot:run
mvn -pl orchestrator spring-boot:run
```

## Deliberate prototype trade-offs
H2 is used locally to keep setup fast; production would use PostgreSQL and Flyway. The first increment uses in-memory orchestration state; production would persist workflow/node/event/decision state. Agent interfaces are provider-neutral so an LLM provider can be added without coupling orchestration semantics to a model SDK.
