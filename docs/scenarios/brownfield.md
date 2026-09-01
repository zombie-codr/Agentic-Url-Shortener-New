# Brownfield Scenario – Add URL Expiration

## Requirement

Add optional expiration support to the existing URL shortener.

Existing URLs that do not have an expiration value must continue to behave exactly as they do today.

---

## Existing System Reasoning

Before proposing a change, the codebase-analysis stage evaluates the existing redirect path and identifies where expiration belongs.

Current redirect flow:

```text
GET /{shortCode}
    ↓
UrlController.redirect()
    ↓
UrlShortenerService.resolve()
    ↓
ShortUrlRepository.findByShortCode()
    ↓
increment click analytics
    ↓
HTTP 302 redirect
```

The existing repository lookup is sufficient to retrieve the URL. Expiration is therefore treated as a domain/service concern rather than introducing a new repository query.

---

## Brownfield Impact Analysis

### Impacted Artifacts

| Artifact                       | Change | Risk   | Reason                                                                       |
| ------------------------------ | ------ | ------ | ---------------------------------------------------------------------------- |
| `ShortUrl.java`                | MODIFY | MEDIUM | Expiration must be persisted as part of the short URL lifecycle.             |
| `UrlDtos.java`                 | MODIFY | LOW    | URL creation must optionally accept an expiration timestamp.                 |
| `UrlShortenerService.java`     | MODIFY | HIGH   | Redirect behavior must reject expired URLs before analytics are incremented. |
| `UrlExpiredException.java`     | ADD    | LOW    | Expiration introduces a distinct domain failure.                             |
| `GlobalExceptionHandler.java`  | MODIFY | LOW    | Expired URLs should map to HTTP `410 Gone`.                                  |
| `UrlShortenerServiceTest.java` | MODIFY | MEDIUM | Existing and new expiration behavior require regression coverage.            |

### Explicitly Unaffected Artifacts

`ShortUrlRepository.findByShortCode()` remains unchanged because the existing lookup returns all information required to determine expiration.

`ShortCodeGenerator` remains unchanged because URL expiration does not affect code generation.

`AnalyticsResponse` remains unchanged because expiration does not alter the analytics response contract.

Explicitly identifying unaffected components reduces unnecessary changes and lowers regression risk.

---

## Proposed Data Flow

The new redirect path becomes:

```text
GET /{shortCode}
    ↓
UrlController.redirect()
    ↓
UrlShortenerService.resolve()
    ↓
ShortUrlRepository.findByShortCode()
    ↓
check expiration
    ├── expired
    │      ↓
    │   UrlExpiredException
    │      ↓
    │   HTTP 410 Gone
    │
    └── active
           ↓
       increment click analytics
           ↓
       HTTP 302 redirect
```

The important sequencing decision is that expiration is checked **before** analytics are incremented.

---

## Regression Risks

The brownfield analysis identifies the following behaviors that must be preserved:

1. Existing URLs without an expiration value must continue to redirect successfully.
2. Missing short codes must continue to return HTTP `404`.
3. Existing custom alias behavior must remain unchanged.
4. Active URLs must continue to increment click analytics.
5. Expired URLs must not increment click analytics.
6. Existing persisted rows without `expiresAt` must remain backward compatible.
7. Expiration checks must use a consistent clock and timezone.

These constraints are propagated from the codebase-analysis stage into planning and implementation.

---

## Required Validation

The validation stage expects evidence for the following scenarios:

* create URL without `expiresAt`
* create URL with a future `expiresAt`
* reject creation with a past `expiresAt`
* redirect an active URL
* return HTTP `410` for an expired URL
* verify expired URLs do not increment analytics
* preserve HTTP `404` for missing URLs
* verify active URLs continue to increment analytics

---

## Agent Execution and Context Lineage

The brownfield workflow is not implemented as independent prompts.

Each stage consumes structured output from the previous stage through the shared `EngineeringContext`.

```text
Requirement Agent
        ↓
BrownfieldCodebaseAnalysisAgent
        ↓
BrownfieldImpactAnalysis
        ↓
ImpactPlannerAgent
        ↓
ChangePlan
        ↓
BrownfieldImplementationAgent
        ↓
ImplementationProposal
        ↓
BrownfieldValidationAgent
        ↓
ValidationReport
        ↓
Security Review
        ↓
Human Release Approval
```

Examples of context propagation:

```java
context.output("codebase-analysis")
```

is consumed by the planning stage.

```java
context.output("impact-plan")
```

is consumed by the implementation stage.

The validation stage consumes both the original impact analysis and the implementation proposal.

This preserves decision lineage across the workflow instead of allowing every agent to independently reinterpret the original requirement.

---

## Validation Evidence

The `BrownfieldValidationAgent` produces a structured `ValidationReport` containing:

* validation checks performed
* evidence supporting those checks
* overall pass/fail status
* residual risks that remain outside the prototype

Examples of validation evidence include:

* `UrlShortenerService.java` is part of the proposed change set
* expiration-specific test requirements were identified
* expired redirects are protected from analytics increments
* existing HTTP `404` behavior is preserved
* API exception handling includes expired URL behavior

The prototype intentionally records residual risks instead of treating validation as binary success.

Current residual risks include:

* Maven/CI execution is not yet performed directly by the validation agent
* PostgreSQL migration compatibility requires integration validation
* concurrent redirect behavior requires load/concurrency testing

---

## Controlled Autonomy

Agents are allowed to analyze and propose engineering actions, but they do not control workflow lifecycle transitions.

The orchestration engine owns:

* dependency resolution
* state transitions
* retry budgets
* policy validation
* approval gates
* safe-stop behavior
* audit events
* shared context propagation

This separates agent reasoning from orchestration authority.

---

## Human Approval Gate

Even after implementation and validation succeed, the release stage is configured as an explicit human approval checkpoint.

The workflow transitions to:

```text
WAITING_APPROVAL
```

rather than completing automatically.

A reviewer must approve the release before the workflow can proceed.

This demonstrates controlled autonomy for a potentially production-impacting action.

---

## Failure Handling

Validation or implementation failures are retried only within the configured retry budget.

If the retry budget is exhausted, the orchestration engine transitions the workflow to a safe-stop state rather than continuing execution.

This prevents uncontrolled retry loops and prevents downstream stages from executing on invalid output.

---

## Auditability

The workflow records audit events for meaningful lifecycle transitions, including:

* `STARTED`
* `SUCCEEDED`
* `FAILED`
* `APPROVAL_REQUIRED`
* `APPROVED`
* `SAFE_STOP`

The brownfield integration test verifies that codebase analysis succeeds and that the release stage generates an `APPROVAL_REQUIRED` audit event.

---

## Test Evidence

`BrownfieldWorkflowTest` validates the complete brownfield orchestration path.

It verifies that:

* structured codebase analysis is produced
* impacted artifacts are identified
* unaffected artifacts are explicitly identified
* regression risks are captured
* the impact plan consumes codebase-analysis output
* implementation produces a structured proposal
* validation produces a structured report
* validation succeeds
* the workflow stops at the human release gate
* meaningful audit events are recorded

This test provides end-to-end evidence that the brownfield workflow is executing real structured agent stages rather than generic placeholder outputs.

---

## Current Limitation

The implementation agent currently produces a structured `ImplementationProposal`; it does not directly mutate repository files.

This is an intentional control boundary in the prototype.

A future iteration could introduce a controlled workspace executor that:

1. applies an approved implementation proposal,
2. runs compilation and tests,
3. captures build output as validation evidence,
4. rolls back or safe-stops if validation fails.

This would extend the current agentic workflow from engineering proposal generation into controlled workspace mutation.
