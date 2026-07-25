# Project Overview

## Document Information

| Field | Value |
| --- | --- |
| Document | Project Overview |
| Status | Draft |
| Version | 1.0.0 |
| Last Updated | 2026-07-16 |
| Owner | Product & Engineering |

## Table of Contents

- [Purpose](#purpose)
- [Project Description](#project-description)
- [Vision](#vision)
- [Objectives](#objectives)
- [Target Users](#target-users)
- [Core Modules](#core-modules)
- [Technology Stack](#technology-stack)
- [Project Scope](#project-scope)
- [Success Criteria](#success-criteria)
- [Notes](#notes)

## Purpose

This document introduces LifeOS to anyone joining the project — engineers, designers, or stakeholders — and serves as the entry point into the rest of the documentation set in `docs/`. It summarizes what LifeOS is, who it is for, and how the documentation is organized, without duplicating the detail owned by other documents.

## Project Description

LifeOS is a mobile-first, AI-powered life management application. It unifies four areas that are normally handled by separate, disconnected apps — travel planning, task management, scheduling, and personal assistance — into a single, cohesive ecosystem. The mobile client is built with Kotlin and Compose Multiplatform, backed by a NestJS API and PostgreSQL database, with Google's Gemini API providing the AI capabilities that sit at the core of the product experience.

Engineering rules for the entire codebase are defined in [CLAUDE.md](../CLAUDE.md), the permanent engineering guide. This document and the rest of `docs/` describe *what* is being built; `CLAUDE.md` describes *how* it must be built.

## Vision

The long-term product vision is maintained in [02-product-vision.md](02-product-vision.md).

## Objectives

- Deliver a single, coherent application that replaces the need for separate travel, task, and calendar apps for day-to-day life management.
- Make Gemini-powered AI assistance a first-class, integrated part of every module rather than a bolted-on chatbot.
- Establish a codebase that follows Clean Architecture, feature-first organization, and MVVM from the first commit, as defined in [CLAUDE.md](../CLAUDE.md).
- Ship a focused MVP (see [10-mvp.md](10-mvp.md)) and evolve it through a documented roadmap (see [11-roadmap.md](11-roadmap.md)).

## Target Users

Detailed personas are documented in [03-user-personas.md](03-user-personas.md). In summary, LifeOS targets individuals who manage a busy personal and professional life — including travel — and want a single, intelligent assistant rather than several disconnected apps.

## Core Modules

| Module | Description |
| --- | --- |
| Authentication | Account creation, sign-in, session management, and account security. |
| Home Dashboard | The user's daily landing screen, surfacing a personalized, AI-curated summary across all other modules. |
| AI Assistant | The conversational AI surface, powered by the Gemini API, that users can consult directly and that powers intelligent features across other modules. |
| Travel | Trip planning and itinerary management. |
| Planner | Task, list, and schedule management. |
| Profile | User profile, preferences, and account settings. |

Functional detail for each module is defined in [07-functional-requirements.md](07-functional-requirements.md); their screens are catalogued in [05-screen-inventory.md](05-screen-inventory.md).

## Technology Stack

The authoritative technology stack and the rules governing its use are defined in [CLAUDE.md](../CLAUDE.md#tech-stack). At a glance:

| Layer | Technology |
| --- | --- |
| Mobile Client | Kotlin, Compose Multiplatform, Material 3 |
| Backend | NestJS |
| Database | PostgreSQL |
| Infrastructure | Docker |
| Security | JWT Authentication |
| API Documentation | Swagger |
| AI | Gemini API |

## Project Scope

In scope for the current phase of the project:

- The six core modules listed above, at the depth defined in [10-mvp.md](10-mvp.md).
- A Kotlin/Compose Multiplatform mobile client and a NestJS backend, containerized with Docker.
- Gemini-powered AI assistance integrated across the AI Assistant, Home Dashboard, Travel, and Planner modules, as defined in [09-ai-features.md](09-ai-features.md).

Explicitly out of scope for the current phase is tracked in [10-mvp.md](10-mvp.md#out-of-scope) and revisited in [11-roadmap.md](11-roadmap.md).

## Success Criteria

Success is measured against the criteria defined in [10-mvp.md](10-mvp.md#success-metrics). This document does not restate them to avoid drift between the two.

## Notes

This document is a summary and navigation aid. When in doubt about a specific detail, defer to the dedicated document listed above rather than this overview.
