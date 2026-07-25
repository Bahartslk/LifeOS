import { Body, Controller, HttpCode, HttpStatus, Post, UseGuards } from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../../../common/decorators/current-user.decorator';
import { NaturalLanguageInputDto } from '../../../common/dto/natural-language-input.dto';
import { JwtAuthGuard } from '../../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../../common/strategies/jwt.strategy';
import { ApiAiProviderErrorResponses } from '../../ai/swagger/ai-provider-error-responses.decorator';
import { BudgetAnalysisResponseDto } from './dto/budget-analysis-response.dto';
import { ItineraryGenerationResponseDto } from './dto/itinerary-generation-response.dto';
import { PackingListResponseDto } from './dto/packing-list-response.dto';
import { RiskAnalysisResponseDto } from './dto/risk-analysis-response.dto';
import { TravelTagsInputDto } from './dto/travel-tags-input.dto';
import { TravelTagsResponseDto } from './dto/travel-tags-response.dto';
import { TripDraftResponseDto } from './dto/trip-draft-response.dto';
import { TripIdInputDto } from './dto/trip-id-input.dto';
import { TravelAiService } from './travel-ai.service';

/**
 * Sprint 20 (Intelligent Travel) — six Travel-owned AI capability
 * endpoints under `/travel/ai/...`, distinct from `AiModule`'s own
 * `/ai/travel/suggest` (Sprint 18B) — this controller lives in, and is
 * registered by, `TravelModule`, per this sprint's explicit "Travel owns
 * its AI capabilities" instruction; it reuses `AiCapabilityService` (via
 * `TravelAiService`) rather than duplicating any AI infrastructure. Thin
 * per CLAUDE.md's "business logic stays in use cases, not controllers" —
 * every handler is parse-DTO, call `TravelAiService`, return. Guarded the
 * same way every other controller in this codebase is.
 *
 * None of these routes ever writes to `Trip`/`ItineraryItem` — every
 * response is a draft or suggestion; the client decides whether to turn
 * one into a real `POST`/`PATCH /travel/trips`/`.../itinerary` call.
 */
@ApiTags('travel')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('travel/ai')
export class TravelAiController {
  constructor(private readonly travelAiService: TravelAiService) {}

  @Post('trips/draft')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Turn natural language into a structured trip draft (not saved)' })
  @ApiOkResponse({ type: TripDraftResponseDto })
  @ApiAiProviderErrorResponses()
  createTripDraft(
    @CurrentUser() user: JwtPayload,
    @Body() dto: NaturalLanguageInputDto,
  ): Promise<TripDraftResponseDto> {
    return this.travelAiService.createTripDraft(user.sub, dto.text);
  }

  @Post('itinerary')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Generate a day-by-day itinerary for an existing trip (not saved)' })
  @ApiOkResponse({ type: ItineraryGenerationResponseDto })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  @ApiAiProviderErrorResponses()
  generateItinerary(
    @CurrentUser() user: JwtPayload,
    @Body() dto: TripIdInputDto,
  ): Promise<ItineraryGenerationResponseDto> {
    return this.travelAiService.generateItinerary(user.sub, dto.tripId, dto.notes);
  }

  @Post('budget-analysis')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Estimate a budget breakdown for an existing trip' })
  @ApiOkResponse({ type: BudgetAnalysisResponseDto })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  @ApiAiProviderErrorResponses()
  analyzeBudget(
    @CurrentUser() user: JwtPayload,
    @Body() dto: TripIdInputDto,
  ): Promise<BudgetAnalysisResponseDto> {
    return this.travelAiService.analyzeBudget(user.sub, dto.tripId, dto.notes);
  }

  @Post('packing-list')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Generate a categorized packing list for an existing trip' })
  @ApiOkResponse({ type: PackingListResponseDto })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  @ApiAiProviderErrorResponses()
  suggestPacking(
    @CurrentUser() user: JwtPayload,
    @Body() dto: TripIdInputDto,
  ): Promise<PackingListResponseDto> {
    return this.travelAiService.suggestPacking(user.sub, dto.tripId, dto.notes);
  }

  @Post('risk-analysis')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: "Analyze an existing trip's itinerary for scheduling/budget/preparation risk",
  })
  @ApiOkResponse({ type: RiskAnalysisResponseDto })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  @ApiAiProviderErrorResponses()
  analyzeRisk(
    @CurrentUser() user: JwtPayload,
    @Body() dto: TripIdInputDto,
  ): Promise<RiskAnalysisResponseDto> {
    return this.travelAiService.analyzeRisk(user.sub, dto.tripId, dto.notes);
  }

  @Post('tags')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Generate suggested category tags for a trip' })
  @ApiOkResponse({ type: TravelTagsResponseDto })
  @ApiAiProviderErrorResponses()
  suggestTags(
    @CurrentUser() user: JwtPayload,
    @Body() dto: TravelTagsInputDto,
  ): Promise<TravelTagsResponseDto> {
    return this.travelAiService.suggestTags(user.sub, dto);
  }
}
