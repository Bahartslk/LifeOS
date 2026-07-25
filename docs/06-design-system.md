# Design System

## Document Information

| Field | Value |
| --- | --- |
| Document | Design System |
| Status | Draft |
| Version | 2.0.0 |
| Last Updated | 2026-07-16 |
| Owner | Design |

## Table of Contents

- [Purpose](#purpose)
- [Design Principles](#design-principles)
- [Color Palette](#color-palette)
- [Typography](#typography)
- [Spacing and Layout](#spacing-and-layout)
- [Components](#components)
- [Iconography](#iconography)
- [Accessibility](#accessibility)
- [Notes](#notes)

## Purpose

This document defines the visual and interaction language for LifeOS, implemented with Material 3 on Compose Multiplatform, per the rules in [CLAUDE.md](../CLAUDE.md#compose-multiplatform-guidelines). It is the shared reference between the Stitch designs in `design/stitch/` and the mobile implementation in `mobile/`.

## Design Principles

- **Premium and calm** — the UI should feel considered and uncluttered, reflecting the "premium" positioning in [02-product-vision.md](02-product-vision.md), not a dense utility tool.
- **Consistent across modules** — Travel, Planner, AI Assistant, and Profile share the same components and interaction patterns rather than each inventing their own.
- **AI presence without intrusion** — AI entry points (e.g., "Ask AI") are visible but never block core task/trip workflows, consistent with [09-ai-features.md](09-ai-features.md#ai-must-never-directly-manipulate-ui).
- **Material 3 as the foundation** — LifeOS extends Material 3's dynamic color and component system rather than replacing it with custom primitives.
- **Turkish-language UI** — all user-facing text (labels, placeholders, dialogs, snackbars, navigation titles) is in Turkish; code identifiers remain in English. Components take their visible copy as parameters from the screen that uses them — the design system itself only centralizes the handful of strings a component needs internally as a default (e.g. a password-visibility toggle's accessibility label), in `DesignSystemStrings`.

## Color Palette

Color tokens are now extracted directly from the Stitch design source (`design/stitch/`) and implemented as a Material 3 `ColorScheme` in `mobile/composeApp/.../core/designsystem/theme/Color.kt`. The brand identity is a premium violet-to-purple gradient (splash background, primary buttons, the Home "Intelligent Hub" card, AI chat bubbles — see `Gradient.kt`) with a teal/emerald secondary accent for success and positive-metric surfaces.

| Token Group | Purpose | Source |
| --- | --- | --- |
| `primary` / `onPrimary` | Violet brand color for buttons, links, and active states. | login.png, onboarding-2/3.png |
| `primaryContainer` / `onPrimaryContainer` | Light violet tonal surface for icon circles and eyebrow chips. | onboarding-1.png "EXECUTIVE FUNCTION HUB" |
| `secondary` / `onSecondary` | Muted neutral-violet for lower-emphasis UI. | — |
| `tertiary` / `onTertiary` | Teal accent for success/weather/productivity surfaces and AI attribution text ("LifeOS Engine"). | home.png, ai-assistent.png |
| `error` / `onError` / `errorContainer` | Validation states and the "high priority" status tag. | home.png "HIGH PRIORITY" |
| `surface` / `onSurface` | Light lavender-gray screen background and near-black headline text. | all screens |
| `outline` | Text field borders, dividers, suggestion-chip borders. | login.png, create-travel.png |

Both a light and a dark `ColorScheme` are defined and kept in parity, per [Dark Mode Support](#accessibility) — profile.png shows a "Karanlık Mod" switch confirming dark mode is an explicit, user-facing setting.

## Typography

The Stitch designs use a bold, rounded, geometric display face for headlines ("Welcome back", "Plan smarter, stay organized, and achieve more."), mapped onto the standard Material 3 type scale (`displayLarge` through `labelSmall`) rather than a custom scale, so text styling stays consistent as new screens are added. No custom font file is bundled yet (see `Type.kt`); the family is a single swappable constant pending a licensed brand typeface.

| Role | Material 3 Style | Usage |
| --- | --- | --- |
| Display headlines | `displayLarge` / `displayMedium` | Onboarding and Login hero copy ("Welcome back", "Plan smarter..."). |
| Screen titles | `headlineSmall` / `headlineMedium` | Top app bar titles (e.g., "My Trips", "Task List"). |
| Section headers | `titleMedium` / `titleLarge` | Section headers within a screen (e.g., "Today's Priority" on the Dashboard). |
| Body text | `bodyMedium` / `bodyLarge` | Primary content text, task/trip descriptions, AI messages. |
| Labels and captions | `labelMedium` / `labelSmall` | Metadata such as due dates, timestamps, and status chips. |
| Eyebrow / overline | Custom `LifeOSTextStyles.overline` | Uppercase, wide-tracking labels above headlines ("UPCOMING TRIP", "READY TO LAUNCH") — Material 3 has no built-in slot for this role. |
| Stat display | Custom `LifeOSTextStyles.statDisplay` | Large bold numerals ("18/24", "92%", "126"). |

## Spacing and Layout

- A base spacing unit of 4dp is used, with common spacing values of 8, 12, 16, 24, and 32dp (`LifeOSSpacing`).
- Screen content uses a consistent horizontal padding (16dp) across modules.
- List and card layouts use consistent vertical rhythm (8dp between related items, 24dp between sections).
- Layouts adapt responsively per [Responsive Layouts](#responsive-layouts-1) below rather than using fixed dimensions.
- Component sizing (button height, icon size, avatar size, FAB size) is a separate token set (`LifeOSSize`) from spacing, so "the gap between things" and "the size of a thing" never get conflated.
- Corner radii are deliberately larger than Material 3's own defaults (`LifeOSShapes`: 8/12/16/20/28dp) with a dedicated fully-rounded pill shape for buttons, chips, and badges — every Stitch screen favors soft, rounded surfaces over sharp ones.
- Primary buttons and the FAB sit on a soft violet-tinted glow rather than a neutral gray shadow (`Modifier.lifeOSGlow()`), reproducing the premium look of the "Login" and FAB buttons.

## Components

Shared components live in `mobile/composeApp/.../core/designsystem/components/` and are built once, reused per [CLAUDE.md](../CLAUDE.md#coding-standards):

| Component | Used In |
| --- | --- |
| Primary / Secondary / Outlined Button | Every form submission and confirmation; three flavors matching login.png, onboarding-2/3.png |
| Text Field / Password Text Field / Search Field | Sign In, Sign Up, Create Trip, Create Task, Edit Profile, in-app search |
| Card / Gradient Card | Trip summary cards, task cards, dashboard summary cards, and "hero" AI-powered surfaces (Intelligent Hub, chat bubbles) |
| Section Header | Grouped content headings with an optional trailing action ("Manage All") |
| Status Chip / Eyebrow Chip / Suggestion Chip | Trip/task status and priority, uppercase overline labels, tappable AI follow-up prompts |
| Badge / AI Badge | Solid status badges on imagery ("COMPLETED") and the violet "AI Optimized Plan" badge |
| Empty State | My Trips, Task List, AI Conversation History when no data exists |
| Loading Indicator / AI Thinking Indicator | Generic network loading, and the branded multi-segment "AI is working" bar (create-travel.png) |
| Skeleton Loader | Content placeholders while a list/card is loading |
| Error View | A recoverable failure with a retry action |
| Top App Bar / Greeting Top Bar | Standard screen headers, and the Home "Good morning" personalized header |
| Bottom Navigation | The five-destination bar with a filled-pill active indicator |
| Floating Action Button | The primary create action (Home, Planner) |
| Modal Bottom Sheet | Contextual actions/forms that shouldn't take over the full screen |
| Dialog / Confirmation Dialog | Destructive actions (delete trip, delete task) |
| Snackbar | Transient confirmations and undo actions |

Components map to screens defined in [05-screen-inventory.md](05-screen-inventory.md#screen-list).

## Iconography

- Material Icons is used as the default icon set to remain consistent with Material 3, wrapped by a single `AppIcon` composable that standardizes size and requires a conscious content-description decision (real label or explicit `null` for decorative icons).
- Icons are used consistently per meaning across modules (e.g., the same "add" icon is used for creating a trip and creating a task).
- AI-related actions use a distinct violet gradient treatment (`AiBadge`, `GradientCard`) so AI entry points are consistently recognizable across Home Dashboard, Travel, and Planner, rather than relying on icon choice alone.

## Accessibility

- All interactive elements provide content descriptions for screen readers.
- Color contrast meets WCAG 2.1 AA for both light and dark color schemes.
- Minimum touch target size of 48x48dp for interactive elements.
- Dark mode is a first-class, fully supported theme, not an inverted afterthought — both schemes are designed and QA'd together.

### Responsive Layouts

- Layouts use adaptive containers so screens render correctly across phone form factors and larger devices, in line with Compose Multiplatform's target platforms.
- Lists and forms reflow rather than clip content on narrower widths.

## Notes

The Stitch design source in `design/stitch/` is the visual source of truth for exact spacing, color values, and component states. This document defines the system-level rules that those designs must follow and that the implementation in `mobile/` must respect.
