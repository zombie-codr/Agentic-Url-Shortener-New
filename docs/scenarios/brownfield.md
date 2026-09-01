# Brownfield Scenario
Requirement: Add expiration support to the existing URL shortener.

Existing flow
GET /{code}
→ Controller
→ Service
→ Repository
→ analytics
→ redirect

New flow
GET /{code}
→ Controller
→ Service
→ Repository
→ expiration check
├─ expired → 410
└─ active → analytics → redirect