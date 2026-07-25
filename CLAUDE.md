# LifeOS Overview

LifeOS is a personal life management platform combining a Kotlin/Compose Multiplatform client with a NestJS backend and integrated AI capabilities. This document is the permanent engineering guide for Claude Code throughout the LifeOS development lifecycle. It defines architecture, conventions, and responsibilities that must be respected in every change made to this codebase.

# Product Vision

Detailed product vision is maintained in [docs/02-product-vision.md](docs/02-product-vision.md). This file governs *how* the product is built; that file governs *what* the product is.

# Tech Stack

| Layer | Technology |
| --- | --- |
| Mobile / Client | Kotlin |
| Mobile / Client | Compose Multiplatform |
| Mobile / Client | Material 3 |
| Mobile / Client | Koin (Dependency Injection) |
| Mobile / Client | Kotlinx Serialization |
| Mobile / Client | Ktor Client (Networking) |
| Mobile / Client | Coil (Image Loading) |
| Mobile / Client | Navigation (Compose Multiplatform Navigation) |
| Backend | NestJS |
| Backend | Prisma (ORM) |
| Database | PostgreSQL |
| Infrastructure | Docker |
| Security | JWT Authentication |
| API Documentation | Swagger |
| AI | Gemini API |

# Architecture

The following architectural rules are mandatory across the project:

- **Clean Architecture** — strict separation between presentation, domain, and data layers. Dependencies point inward; outer layers depend on inner layers, never the reverse.
- **Feature-first architecture** — code is organized by feature/module, not by technical layer. Each feature owns its presentation, domain, and data code.
- **MVVM** — the client uses ViewModels to expose state and handle presentation logic; Composables remain declarative and stateless where possible.
- **Repository Pattern** — data access is abstracted behind repository interfaces; consumers never talk to data sources (network, database) directly.
- **Dependency Injection** — dependencies are provided, not constructed inline. No manual `new`/instantiation of services inside consumers.
- **SOLID Principles** — applied consistently across both mobile and backend codebases.

# Project Structure

```
mobile/
backend/
docs/
design/
docker/
```

- **mobile/** — Kotlin / Compose Multiplatform client application. Contains feature modules, shared UI components, and platform-specific entry points.
- **backend/** — NestJS backend application. Contains API modules, business logic, database access, and authentication.
- **docs/** — Project documentation (vision, requirements, architecture, guidelines). Source of truth for product and technical decisions.
- **design/** — Design assets, UI references, and design system source files.
- **docker/** — Docker configuration for local development and deployment (containers, compose files, environment setup).

# Coding Standards

- Use meaningful, descriptive naming for variables, functions, classes, and files.
- Keep functions small and focused on a single responsibility.
- Build reusable components instead of one-off implementations.
- Avoid duplicated code; extract shared logic into common utilities or modules.
- Prefer composition over inheritance.
- Keep files modular — one clear responsibility per file.
- Follow official Kotlin conventions and style guidelines.
- Follow NestJS best practices (modules, providers, decorators, dependency injection).

# Compose Multiplatform Guidelines

- Use Material 3 components and theming as the UI foundation.
- Build reusable, composable UI components rather than duplicating UI code across screens.
- Apply state hoisting — Composables receive state and event callbacks from ViewModels; they do not own business state.
- Use a centralized navigation structure; avoid ad hoc navigation logic scattered across screens.
- Support dark mode across all screens and components.
- Follow accessibility best practices (content descriptions, sufficient contrast, touch target sizing).
- Design layouts to be responsive across different screen sizes and orientations.

# Backend Guidelines

- Expose functionality through REST APIs following consistent resource-oriented conventions.
- Protect endpoints using JWT Authentication.
- Validate all incoming requests before they reach business logic.
- Use DTOs for all request and response payloads; never expose internal entities directly.
- Document all endpoints with Swagger.
- Handle errors consistently with structured error responses and appropriate HTTP status codes.
- Maintain clear layer separation: controllers, services, repositories/data access — no layer skipping.

# Database Guidelines

- Use PostgreSQL as the primary database.
- Use UUIDs as primary keys for all entities.
- Apply normalization to avoid redundant or inconsistent data.
- Define foreign keys explicitly to enforce referential integrity.
- Add indexes for columns used in frequent lookups and joins.
- Use soft delete where appropriate instead of hard-deleting records that require history or recovery.

# AI Integration Rules

- All AI functionality is powered by the Gemini API.
- AI integration must be modular — isolated behind dedicated services/modules, not scattered across the codebase.
- AI must never directly manipulate UI; AI output flows through the same state and data layers as any other data source.
- Business logic stays inside use cases, not inside AI prompt handling or AI service code.
- Prompt templates must be reusable and centrally maintained, not duplicated inline across call sites.

# Documentation Rules

- Every new feature must include corresponding documentation in `docs/`.
- Documentation must be kept synchronized with the implementation — outdated docs are treated as bugs.
- Documentation changes should be part of the same change set as the feature they describe.

# Git Workflow

- Use feature branches for all new work; do not commit directly to the main branch.
- Keep commits small and focused on a single logical change.
- Write meaningful commit messages that describe the intent of the change, not just the files touched.

# General Development Principles

- Think before coding.
- Prioritize maintainability.
- Keep the project scalable.
- Do not over-engineer.
- Prefer readability.
- Write production-quality code.

# Claude Code Responsibilities

- Generate production-ready code.
- Respect project architecture at all times.
- Never ignore existing documentation.
- Never invent project requirements.
- Always keep consistency with existing patterns and conventions.
- Always reuse existing components before creating new ones.
- Always explain major architectural decisions before implementing them.
