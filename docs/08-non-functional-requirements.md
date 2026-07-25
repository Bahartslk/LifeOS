# Non-Functional Requirements

## Document Information

| Field | Value |
| --- | --- |
| Document | Non-Functional Requirements |
| Status | Draft |
| Version | 1.1.0 |
| Last Updated | 2026-07-16 |
| Owner | Engineering |

## Table of Contents

- [Purpose](#purpose)
- [Performance](#performance)
- [Scalability](#scalability)
- [Security](#security)
- [Reliability](#reliability)
- [Usability](#usability)
- [Maintainability](#maintainability)
- [Compliance](#compliance)
- [Notes](#notes)

## Purpose

This document defines the quality attributes LifeOS must satisfy, complementing the functional scope in [07-functional-requirements.md](07-functional-requirements.md). These are engineering targets to design and test against, not measurements of a shipped system.

## Performance

| Target | Requirement |
| --- | --- |
| API response time | p95 latency under 300ms for standard CRUD endpoints (excluding AI endpoints). |
| AI Assistant response time | First response token/content surfaced to the user within 3 seconds under normal conditions; the UI must show a loading state for any longer wait rather than appearing frozen. |
| App cold start | Home Dashboard interactive within 2 seconds on a mid-tier device under normal network conditions. |
| List rendering | Trip and task lists render smoothly (no dropped frames under normal load) using paginated or lazy-loaded data rather than loading an entire history at once. |

## Scalability

- The backend (NestJS) must be stateless at the application layer so additional instances can be added behind a load balancer without code changes.
- Database access must go through the repository layer (per [CLAUDE.md](../CLAUDE.md#architecture)) so query optimization and read/write scaling can be introduced without touching business logic.
- Gemini API calls must be isolated behind a dedicated AI module (see [09-ai-features.md](09-ai-features.md)) so rate limiting, retries, or provider changes do not ripple through other modules.
- The concrete scale-out topology (connection pooling, caching, AI job queue) and the phase at which each is introduced is defined in [12-project-architecture.md](12-project-architecture.md#scalability--production-readiness).

## Security

- All authenticated endpoints require a valid JWT, per [CLAUDE.md](../CLAUDE.md#backend-guidelines) and [15-api-design.md](15-api-design.md#authentication).
- Passwords are never stored or logged in plaintext; only salted hashes are persisted.
- All network traffic between the mobile client and backend uses HTTPS/TLS.
- Refresh tokens are revocable; signing out or revoking a session must invalidate the corresponding refresh token server-side.
- Input validation is enforced on every request DTO before reaching business logic, per [CLAUDE.md](../CLAUDE.md#backend-guidelines).
- AI prompts sent to the Gemini API must exclude data the user has not authorized the assistant to access (e.g., only the requesting user's own trips and tasks).
- Concrete mechanisms (JWT signing algorithm and rotation, password hashing algorithm, secrets management) are defined in [12-project-architecture.md](12-project-architecture.md#security-architecture).

## Reliability

- The system distinguishes between recoverable errors (e.g., AI provider timeout) and unrecoverable errors (e.g., invalid input), returning structured error responses per [15-api-design.md](15-api-design.md#error-handling).
- Core flows (Authentication, Travel, Planner) must degrade gracefully if the AI Assistant is unavailable — non-AI functionality must continue to work.
- Data-modifying operations (create/update/delete trip, task) must be atomic at the database transaction level.

## Usability

- The application follows the Material 3 interaction patterns defined in [06-design-system.md](06-design-system.md) consistently across all six modules.
- Primary actions (create trip, create task, ask AI) must be reachable within two taps from the Home Dashboard.
- Error and empty states must be informative, not generic (e.g., "No trips yet — create your first trip" rather than a blank screen).

## Maintainability

- The codebase follows Clean Architecture, feature-first organization, MVVM, Repository Pattern, Dependency Injection, and SOLID principles, per [CLAUDE.md](../CLAUDE.md#architecture) and detailed in [12-project-architecture.md](12-project-architecture.md).
- Each module (Authentication, Home Dashboard, AI Assistant, Travel, Planner, Profile) must be independently testable and independently modifiable without requiring changes to unrelated modules.
- All backend endpoints must be documented in Swagger as they are built, per [CLAUDE.md](../CLAUDE.md#backend-guidelines).

## Compliance

- User personal data (profile information, trip and task content) is treated as private data and is only accessible to its owning user via authenticated requests.
- Data sent to the Gemini API is limited to what is necessary to fulfill the specific AI request, consistent with data minimization principles.
- Soft-deleted records (see [14-database-design.md](14-database-design.md#entities)) remain subject to the same access controls as active records.

## Notes

Non-functional targets in this document should be revisited once real usage data is available, and adjusted rather than treated as permanently fixed thresholds.
