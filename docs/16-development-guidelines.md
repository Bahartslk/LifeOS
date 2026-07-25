# Development Guidelines

## Document Information

| Field | Value |
| --- | --- |
| Document | Development Guidelines |
| Status | Draft |
| Version | 1.0.0 |
| Last Updated | 2026-07-16 |
| Owner | Engineering |

## Table of Contents

- [Purpose](#purpose)
- [Coding Standards](#coding-standards)
- [Branching Strategy](#branching-strategy)
- [Commit Conventions](#commit-conventions)
- [Testing Guidelines](#testing-guidelines)
- [Code Review Process](#code-review-process)
- [Documentation Standards](#documentation-standards)
- [Notes](#notes)

## Purpose

This document defines the day-to-day engineering workflow for contributing to LifeOS — how work moves from a task to a merged, documented change. It operationalizes the rules already defined in [CLAUDE.md](../CLAUDE.md) rather than restating them.

## Coding Standards

Coding standards, architecture rules, and per-platform (Compose Multiplatform / NestJS) guidelines are defined once, in [CLAUDE.md](../CLAUDE.md#coding-standards), and are binding for all contributions. This document does not duplicate them; refer to:

- [Coding Standards](../CLAUDE.md#coding-standards)
- [Architecture](../CLAUDE.md#architecture)
- [Compose Multiplatform Guidelines](../CLAUDE.md#compose-multiplatform-guidelines)
- [Backend Guidelines](../CLAUDE.md#backend-guidelines)
- [Database Guidelines](../CLAUDE.md#database-guidelines)
- [AI Integration Rules](../CLAUDE.md#ai-integration-rules)

## Branching Strategy

- `main` is always in a releasable state; no direct commits to `main`.
- All work happens on feature branches, named `feature/<module>-<short-description>` (e.g., `feature/travel-itinerary-editor`), matching the module names in [01-project-overview.md](01-project-overview.md#core-modules).
- Bug fixes use `fix/<short-description>`; documentation-only changes use `docs/<short-description>`.
- Branches are merged into `main` via pull request only, per [Code Review Process](#code-review-process).

## Commit Conventions

Commits follow Conventional Commits:

| Prefix | Use |
| --- | --- |
| `feat:` | New functionality |
| `fix:` | Bug fix |
| `refactor:` | Non-behavioral code change |
| `docs:` | Documentation-only change |
| `test:` | Test additions or changes |
| `chore:` | Tooling, dependencies, configuration |

- Commits are kept small and scoped to a single logical change, per [CLAUDE.md](../CLAUDE.md#git-workflow).
- Commit messages describe intent ("why"), not just the files touched.

## Testing Guidelines

| Layer | Test Type | Scope |
| --- | --- | --- |
| Mobile — Domain | Unit tests | Use cases, tested independently of Compose and networking. |
| Mobile — Presentation | Unit tests | ViewModel state and event handling. |
| Mobile — UI | Compose UI tests | Critical flows per [04-user-flow.md](04-user-flow.md#core-flows) (sign in, create trip, create task). |
| Backend — Services | Unit tests | Business logic per module, with repositories mocked behind their interfaces. |
| Backend — API | End-to-end tests | Endpoint contracts defined in [15-api-design.md](15-api-design.md#endpoints), including auth and validation error cases. |

- New functional requirements (see [07-functional-requirements.md](07-functional-requirements.md)) must ship with corresponding tests in the same change set.
- AI-dependent code paths must be tested with the Gemini API call mocked at the AI module boundary, so tests do not depend on external AI availability.

## Code Review Process

- [ ] Pull request describes the change and links the relevant requirement ID(s) (e.g., `FR-TRAVEL-05`) from [07-functional-requirements.md](07-functional-requirements.md).
- [ ] At least one reviewer approval before merge.
- [ ] Automated checks (build, lint, tests) pass before merge.
- [ ] Architecture rules in [CLAUDE.md](../CLAUDE.md#architecture) are respected (layer separation, DI, no direct data-source access from presentation).
- [ ] Any new or changed endpoint is reflected in Swagger and, if the contract changed, in [15-api-design.md](15-api-design.md).
- [ ] Any new screen or flow is reflected in [05-screen-inventory.md](05-screen-inventory.md) and [04-user-flow.md](04-user-flow.md) if applicable.

## Documentation Standards

Documentation rules are defined once, in [CLAUDE.md](../CLAUDE.md#documentation-rules): every new feature must include documentation, and docs must stay synchronized with implementation. In practice, for LifeOS this means:

- A new or changed functional requirement is reflected in [07-functional-requirements.md](07-functional-requirements.md) in the same pull request.
- A new or changed endpoint is reflected in [15-api-design.md](15-api-design.md) and Swagger in the same pull request.
- A new or changed table/column is reflected in [14-database-design.md](14-database-design.md) in the same pull request (via migration + doc update together).
- A new or changed screen is reflected in [05-screen-inventory.md](05-screen-inventory.md) in the same pull request.

## Notes

This document governs *process*; it intentionally does not restate architectural or coding rules already defined in [CLAUDE.md](../CLAUDE.md), to avoid the two drifting apart.
