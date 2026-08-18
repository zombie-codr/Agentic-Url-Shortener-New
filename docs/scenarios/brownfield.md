# Brownfield Scenario
Requirement: Add expiration support to the existing URL shortener.

The codebase-analysis agent identifies ShortUrl, repository, service, controller, persistence and tests as impacted. The implementation is validated against expired redirect behavior. A failed validation can be retried only within the configured retry budget; exhaustion results in safe stop.
