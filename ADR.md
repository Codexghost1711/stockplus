# ADR: StockPulse Architecture

## 1. Where commerce logic lives
### Context
The project needs a single, explainable commerce decision layer that can support both pricing and reorder suggestions while remaining extensible for future strategies.

### Options
- Keep pricing and reorder logic in controllers/services only.
- Centralize decision logic in a dedicated commerce layer.
- Use a pluggable strategy interface with a shared advisor.

### Decision
To be finalized during implementation: the project will center commerce evaluation in a dedicated commerce package with a common advisor contract.

### Tradeoffs
- Pros: cleaner separation, easier explainability, less duplication.
- Cons: requires a slightly more structured initial design.

## 2. Unified commerce recommendation vs separate pricing/reorder contracts
### Context
The HTTP API and async agent loop should both consume the same recommendation abstraction without duplicating logic.

### Options
- Separate pricing and reorder recommendation services.
- A unified `CommerceRecommendation` carrying both outcomes.

### Decision
To be finalized during implementation: use a unified recommendation model so controller and async flows depend on the same contract.

### Tradeoffs
- Pros: consistent API and easier orchestration.
- Cons: may require careful modeling of optional fields.

## 3. Runtime strategy switching
### Context
The system should let a deployment switch between rule-based and AI-based commerce logic without code deployment or restart.

### Options
- Hardcode a single strategy in service classes.
- Use a registry/factory abstraction bound to configuration properties.

### Decision
To be finalized during implementation: a strategy registry/factory will resolve the active strategy from configuration such as `stockpulse.commerce.strategy=RULE`.

### Tradeoffs
- Pros: easy experimentation and future extension.
- Cons: introduces a small amount of indirection.

## 4. LLM failure handling
### Context
AI recommendations can fail due to malformed output, provider errors, timeouts, or quota limits. The platform still needs predictable behavior.

### Options
- Fail fast and surface errors.
- Fallback to rule-based recommendations.
- Add retry/validation layers.

### Decision
To be finalized during implementation: the AI strategy will be isolated behind `LLMGateway`, with eventual fallback to rule-based recommendations if AI handling fails.

### Tradeoffs
- Pros: more resilient and demo-friendly.
- Cons: requires explicit strategy for degraded mode.

## 5. Agentic loop trigger and decoupling
### Context
The system should react to inventory and demand changes asynchronously instead of blocking the request lifecycle.

### Options
- Trigger recommendation generation directly in controllers.
- Publish domain events handled asynchronously by an agent loop.

### Decision
To be finalized during implementation: the platform will use Spring asynchronous event handling to decouple stock/order changes from recommendation generation.

### Tradeoffs
- Pros: faster HTTP responses and better scalability.
- Cons: asynchronous workflows are harder to debug.

## 6. Extensibility and Sprint 2 seam
### Context
Sprint 2 may add cost-price and supplier-aware logic without major refactoring.

### Options
- Embed Sprint 2 fields directly in the current model.
- Leave extension seams for future pricing and supply data.

### Decision
To be finalized during implementation: the domain model will reserve seams for cost price and supplier identifiers without implementing Sprint 2 behavior yet.

### Tradeoffs
- Pros: low-friction extension.
- Cons: requires careful planning to avoid premature over-engineering.
