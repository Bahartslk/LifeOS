# User Personas

## Document Information

| Field | Value |
| --- | --- |
| Document | User Personas |
| Status | Draft |
| Version | 1.0.0 |
| Last Updated | 2026-07-16 |
| Owner | Product & Design |

## Table of Contents

- [Purpose](#purpose)
- [Persona Summary](#persona-summary)
- [Goals and Motivations](#goals-and-motivations)
- [Pain Points](#pain-points)
- [Behaviors](#behaviors)
- [Needs](#needs)
- [Notes](#notes)

## Purpose

This document defines the primary user personas for LifeOS. These personas ground the decisions made in [04-user-flow.md](04-user-flow.md), [05-screen-inventory.md](05-screen-inventory.md), and [07-functional-requirements.md](07-functional-requirements.md), and should be validated and refined with real user research as the product matures.

## Persona Summary

| Persona | Summary | Primary Modules |
| --- | --- | --- |
| The Mobile Professional | A working professional who travels for both work and leisure and needs travel, tasks, and schedule kept in sync. | Travel, Planner, Home Dashboard |
| The Frequent Traveler | Travels often, plans multi-stop trips in detail, and wants an assistant that can help build and adjust itineraries. | Travel, AI Assistant |
| The Organized Planner | Manages a dense personal and professional schedule and relies on structured task lists and reminders rather than trips. | Planner, Home Dashboard, Profile |

## Goals and Motivations

**The Mobile Professional**
- Keep upcoming trips, deadlines, and daily tasks visible in one place without switching apps.
- Trust that the AI Assistant understands both their schedule and their travel plans when offering help.

**The Frequent Traveler**
- Plan detailed, multi-destination itineraries quickly.
- Get AI-assisted suggestions for itinerary items rather than building every trip from a blank page.

**The Organized Planner**
- Maintain reliable task lists and a clear daily/weekly schedule.
- Receive a concise, prioritized summary of what matters today.

## Pain Points

| Pain Point | Affected Persona(s) |
| --- | --- |
| Travel plans and task lists live in separate apps with no shared context. | Mobile Professional, Frequent Traveler |
| Generic AI assistants have no visibility into personal schedules or trips, making their suggestions shallow. | Mobile Professional, Frequent Traveler |
| Building a detailed itinerary manually is time-consuming. | Frequent Traveler |
| Daily task and schedule overviews require opening multiple screens or apps to assemble. | Organized Planner |

## Behaviors

- Checks a daily summary/dashboard first thing to understand what the day looks like ([Home Dashboard](07-functional-requirements.md#home-dashboard)).
- Uses the AI Assistant conversationally, expecting it to be aware of existing trips and tasks rather than starting from zero context.
- Plans trips in bursts (researching, adding itinerary items) rather than all at once.
- Reviews and adjusts tasks throughout the day rather than only during a single planning session.

## Needs

- A single dashboard that reflects both travel and task state, not just one or the other.
- An AI Assistant that can be asked about trips and tasks directly, with responses grounded in the user's actual data (see [09-ai-features.md](09-ai-features.md)).
- Fast entry points for adding a task or a trip itinerary item, minimizing the steps between intent and captured data.
- Confidence that their account and personal data (schedules, trip details) are secure, per [08-non-functional-requirements.md](08-non-functional-requirements.md#security).

## Notes

These personas are illustrative and derived directly from the product vision in [02-product-vision.md](02-product-vision.md). They should be revisited and validated against real user feedback once the product has active users.
