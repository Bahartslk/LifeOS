import { forwardRef, Module } from '@nestjs/common';
import { PlannerModule } from '../planner/planner.module';
import { TravelModule } from '../travel/travel.module';
import { UsersModule } from '../users/users.module';
import { AiCapabilityService } from './capabilities/ai-capability.service';
import { AiConfigService } from './config/ai-config.service';
import { AI_PROVIDERS } from './constants/ai.constants';
import { ContextBuilder } from './context/context-builder.service';
import { AiProvider } from './interfaces/ai-provider.interface';
import { PromptBuilder } from './prompt/prompt-builder.service';
import { GeminiProvider } from './providers/gemini/gemini.provider';
import { OpenRouterProvider } from './providers/openrouter/openrouter.provider';
import { ProviderRegistry } from './registry/provider-registry.service';
import { ProviderRouter } from './router/provider-router.service';
import { AiController } from './ai.controller';
import { AiFallbackExecutor } from './service/ai-fallback.executor';
import { AiRetryExecutor } from './service/ai-retry.executor';
import { AiService } from './service/ai.service';

/**
 * Sprint 18A (AI Foundation) built the provider-agnostic infrastructure;
 * Sprint 18B (First AI Capabilities) activated it: a second provider
 * (`OpenRouterProvider`), automatic fallback across the provider chain
 * (`AiFallbackExecutor`), feature-based routing (`ProviderRouter`), and
 * this module's first controller — four capability-scoped endpoints under
 * `/ai/...`, not a generic chatbot. Sprint 19 (Intelligent Planner) added a
 * second consumer: `PlannerModule` injects `AiCapabilityService` directly
 * to build its own Planner-owned AI capabilities (`modules/planner/ai/`)
 * on the exact same execution engine. Sprint 20 (Intelligent Travel) added
 * a third, identical consumer: `TravelModule`'s `modules/travel/ai/`. In
 * neither case does this module ever own Planner- or Travel-specific
 * prompts itself.
 *
 * Depends on `UsersModule`/`PlannerModule`/`TravelModule` (via
 * `ContextBuilder`), per "AI may depend on UsersService, PlannerService,
 * TravelService. Nothing else." Both `PlannerModule` and `TravelModule`
 * importing this module back (for `AiCapabilityService`) makes this a
 * genuine, mutual circular module dependency with each — resolved with
 * Nest's standard `forwardRef()` on both sides of both edges (see
 * `planner.module.ts`/`travel.module.ts`) rather than restructured away,
 * since the dependency is real in both directions: this module needs
 * Planner's and Travel's data services for context, each of them now needs
 * this module's execution engine for their own AI features. `forwardRef`
 * only affects module resolution, not the DI graph of concrete providers —
 * no provider here needs `@Inject(forwardRef(...))`.
 *
 * Providers self-register through Nest DI: `GeminiProvider` and
 * `OpenRouterProvider` are both normal Nest providers below (so
 * `AiConfigService` resolves through Nest DI as usual for each), and the
 * `AI_PROVIDERS` factory collects every registered provider instance into
 * the plain array `ProviderRegistry` injects — exactly the pattern Sprint
 * 18A anticipated. `ProviderRegistry`/`ProviderRouter`/`AiFallbackExecutor`/
 * `AiService` never import a concrete provider class or call `new` on one.
 * A third provider (Anthropic, Groq, Azure OpenAI, Ollama, ...) later means:
 * implement `AiProvider`, add the class here and to the `AI_PROVIDERS`
 * factory's `inject`/return list, and list its id in
 * `AI_ENABLED_PROVIDERS`/`AI_PROVIDER_PRIORITY`. Nothing in `registry/`,
 * `router/`, `service/`, `context/`, `prompt/`, or `capabilities/` changes.
 */
@Module({
  imports: [UsersModule, forwardRef(() => PlannerModule), forwardRef(() => TravelModule)],
  controllers: [AiController],
  providers: [
    AiConfigService,
    GeminiProvider,
    OpenRouterProvider,
    {
      provide: AI_PROVIDERS,
      useFactory: (gemini: GeminiProvider, openRouter: OpenRouterProvider): AiProvider[] => [
        gemini,
        openRouter,
      ],
      inject: [GeminiProvider, OpenRouterProvider],
    },
    ProviderRegistry,
    ProviderRouter,
    PromptBuilder,
    ContextBuilder,
    AiRetryExecutor,
    AiFallbackExecutor,
    AiService,
    AiCapabilityService,
  ],
  exports: [AiService, AiCapabilityService],
})
export class AiModule {}
