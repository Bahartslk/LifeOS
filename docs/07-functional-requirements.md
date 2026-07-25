# Functional Requirements

## Document Information

| Field | Value |
| --- | --- |
| Document | Functional Requirements |
| Status | Draft |
| Version | 1.0.0 |
| Last Updated | 2026-07-16 |
| Owner | Engineering |

## Table of Contents

- [Purpose](#purpose)
- [Requirement Overview](#requirement-overview)
- [Functional Requirements List](#functional-requirements-list)
- [Assumptions](#assumptions)
- [Dependencies](#dependencies)
- [Notes](#notes)

## Purpose

This document defines the functional requirements for each of LifeOS's six core modules. Requirements are organized by module and identified with an ID (`FR-<MODULE>-<NUMBER>`) so they can be referenced from the API design ([15-api-design.md](15-api-design.md)), database design ([14-database-design.md](14-database-design.md)), and MVP scope ([10-mvp.md](10-mvp.md)).

## Requirement Overview

| Module | Requirement Count | Related Screens |
| --- | --- | --- |
| Authentication | 6 | [05-screen-inventory.md#authentication](05-screen-inventory.md#authentication) |
| Home Dashboard | 4 | [05-screen-inventory.md#home-dashboard](05-screen-inventory.md#home-dashboard) |
| AI Assistant | 5 | [05-screen-inventory.md#ai-assistant](05-screen-inventory.md#ai-assistant) |
| Travel | 6 | [05-screen-inventory.md#travel](05-screen-inventory.md#travel) |
| Planner | 6 | [05-screen-inventory.md#planner](05-screen-inventory.md#planner) |
| Profile | 5 | [05-screen-inventory.md#profile](05-screen-inventory.md#profile) |

## Functional Requirements List

### Authentication

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-AUTH-01 | Users can create an account with an email and password. | Must |
| FR-AUTH-02 | Users can sign in with an email and password. | Must |
| FR-AUTH-03 | The system issues a JWT access token and refresh token upon successful sign-in. | Must |
| FR-AUTH-04 | Users can request a password reset via email. | Must |
| FR-AUTH-05 | Users can sign out, invalidating their local session. | Must |
| FR-AUTH-06 | The system rejects requests with an expired or invalid JWT and prompts re-authentication. | Must |

### Home Dashboard

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-HOME-01 | The Dashboard displays the user's tasks due today. | Must |
| FR-HOME-02 | The Dashboard displays the user's upcoming trip(s), if any. | Must |
| FR-HOME-03 | The Dashboard surfaces an AI-curated highlight (e.g., a suggestion or reminder) sourced from the AI Assistant. | Should |
| FR-HOME-04 | The Dashboard provides navigation entry points into Travel, Planner, AI Assistant, and Profile. | Must |

### AI Assistant

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-AI-01 | Users can start a conversation with the AI Assistant. | Must |
| FR-AI-02 | The AI Assistant can access the user's relevant trip and task data to ground its responses, per [09-ai-features.md](09-ai-features.md). | Must |
| FR-AI-03 | Users can view a history of past AI conversations. | Should |
| FR-AI-04 | Users can accept an AI suggestion to create or modify a trip itinerary item or task directly from the conversation. | Should |
| FR-AI-05 | The AI Assistant can be reached from Home Dashboard, Trip Details, and Task List, not only from a dedicated tab. | Must |

### Travel

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-TRAVEL-01 | Users can create a trip with a title, destination, and date range. | Must |
| FR-TRAVEL-02 | Users can view a list of their trips, grouped by upcoming, ongoing, and past. | Must |
| FR-TRAVEL-03 | Users can add, edit, and remove itinerary items within a trip. | Must |
| FR-TRAVEL-04 | Users can edit or delete an existing trip. | Must |
| FR-TRAVEL-05 | Users can request AI-generated itinerary suggestions for a trip. | Should |
| FR-TRAVEL-06 | Deleting a trip is a soft delete, recoverable per [14-database-design.md](14-database-design.md#entities). | Must |

### Planner

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-PLANNER-01 | Users can create a task with a title, optional description, and optional due date/time. | Must |
| FR-PLANNER-02 | Users can organize tasks into task lists. | Must |
| FR-PLANNER-03 | Users can mark a task complete or incomplete. | Must |
| FR-PLANNER-04 | Users can view tasks in a calendar view by due date. | Should |
| FR-PLANNER-05 | Users can edit or delete an existing task. | Must |
| FR-PLANNER-06 | Users can set a task priority (low, medium, high). | Should |

### Profile

| ID | Requirement | Priority |
| --- | --- | --- |
| FR-PROFILE-01 | Users can view their profile information. | Must |
| FR-PROFILE-02 | Users can edit their display name and avatar. | Must |
| FR-PROFILE-03 | Users can change their password. | Must |
| FR-PROFILE-04 | Users can configure app preferences (theme, notifications, locale). | Should |
| FR-PROFILE-05 | Users can view and revoke active sessions. | Could |

## Assumptions

- Priority values follow MoSCoW (Must, Should, Could, Won't); MVP scope in [10-mvp.md](10-mvp.md) is drawn primarily from "Must" requirements.
- All requirements above assume a single-user account model; multi-user/shared trips or task lists are not assumed and would require separate requirements if introduced.
- Requirements assume network connectivity for AI Assistant and sync features; offline behavior is addressed as a non-functional concern in [08-non-functional-requirements.md](08-non-functional-requirements.md).

## Dependencies

| Requirement Group | Depends On |
| --- | --- |
| AI Assistant (FR-AI-*) | Gemini API integration, per [09-ai-features.md](09-ai-features.md) |
| Authentication (FR-AUTH-*) | JWT issuance and validation, per [15-api-design.md](15-api-design.md#authentication) |
| Home Dashboard (FR-HOME-*) | Travel and Planner data being available via their respective APIs |
| Travel and Planner soft delete (FR-TRAVEL-06) | Soft delete strategy defined in [14-database-design.md](14-database-design.md) |

## Notes

Requirement IDs are stable identifiers and should not be renumbered once referenced elsewhere (code, tests, or other documents). Deprecated requirements should be marked as such rather than removed, to preserve traceability.
