import { applyDecorators } from '@nestjs/common';
import { ApiResponse } from '@nestjs/swagger';

/**
 * The two provider-failure responses every AI capability endpoint can
 * return, regardless of which module owns the capability (`AiController`,
 * `PlannerAiController`, `TravelAiController`, `DailyBriefController`).
 * Promoted here in Sprint 21.5's architecture audit — the same two
 * `@ApiResponse` objects had been redeclared independently per controller
 * (a local `PROVIDER_ERROR_RESPONSES` array in `travel-ai.controller.ts`
 * and `daily-brief.controller.ts`, and inline duplication in
 * `planner-ai.controller.ts`) — one shared decorator removes that
 * duplication per CLAUDE.md's "avoid duplicated code" rule. Purely a
 * documentation-authoring change: the emitted Swagger output is unchanged.
 */
export function ApiAiProviderErrorResponses(): MethodDecorator & ClassDecorator {
  return applyDecorators(
    ApiResponse({
      status: 503,
      description:
        'No AI provider is currently available, or every provider in the fallback chain failed.',
    }),
    ApiResponse({
      status: 502,
      description:
        "The selected provider's response could not be parsed as the expected JSON shape.",
    }),
  );
}
