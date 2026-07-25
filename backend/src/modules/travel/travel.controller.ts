import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  HttpStatus,
  Param,
  ParseUUIDPipe,
  Patch,
  Post,
  Query,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { TravelService } from './travel.service';
import { ItineraryService } from './itinerary.service';
import { CreateTripDto } from './dto/create-trip.dto';
import { UpdateTripDto } from './dto/update-trip.dto';
import { TripQueryDto } from './dto/trip-query.dto';
import { TripResponseDto } from './dto/trip-response.dto';
import { PaginatedTripsResponseDto } from './dto/paginated-trips-response.dto';
import { CreateItineraryItemDto } from './dto/create-itinerary-item.dto';
import { ItineraryItemResponseDto } from './dto/itinerary-item-response.dto';
import { ReorderItineraryDto } from './dto/reorder-itinerary.dto';
import { TravelDashboardResponseDto } from './dto/dashboard-response.dto';

/**
 * Thin per CLAUDE.md's "business logic stays in use cases, not
 * controllers" rule. Guarded at the controller level, mirroring
 * `PlannerController` exactly. Trip CRUD, the nested itinerary GET/POST,
 * reorder, and the dashboard all live here; `PATCH`/`DELETE
 * /travel/itinerary/:id` (no `tripId` in the path) live in the separate
 * `ItineraryController` below, matching Sprint 16.0's asymmetric route
 * list exactly.
 */
@ApiTags('travel')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('travel')
export class TravelController {
  constructor(
    private readonly travelService: TravelService,
    private readonly itineraryService: ItineraryService,
  ) {}

  @Post('trips')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create a trip' })
  @ApiOkResponse({ type: TripResponseDto })
  createTrip(
    @CurrentUser() user: JwtPayload,
    @Body() dto: CreateTripDto,
  ): Promise<TripResponseDto> {
    return this.travelService.createTrip(user.sub, dto);
  }

  @Get('trips')
  @ApiOperation({ summary: "List the caller's trips (filtered, sorted, cursor-paginated)" })
  @ApiOkResponse({ type: PaginatedTripsResponseDto })
  getTrips(
    @CurrentUser() user: JwtPayload,
    @Query() query: TripQueryDto,
  ): Promise<PaginatedTripsResponseDto> {
    return this.travelService.getTrips(user.sub, query);
  }

  @Get('trips/:id')
  @ApiOperation({ summary: 'Get one trip' })
  @ApiOkResponse({ type: TripResponseDto })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  getTrip(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<TripResponseDto> {
    return this.travelService.getTrip(user.sub, id);
  }

  @Patch('trips/:id')
  @ApiOperation({ summary: 'Update a trip (partial)' })
  @ApiOkResponse({ type: TripResponseDto })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  updateTrip(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
    @Body() dto: UpdateTripDto,
  ): Promise<TripResponseDto> {
    return this.travelService.updateTrip(user.sub, id, dto);
  }

  @Delete('trips/:id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiOperation({ summary: 'Delete a trip (soft delete)' })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  async deleteTrip(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<void> {
    await this.travelService.deleteTrip(user.sub, id);
  }

  @Get('trips/:id/itinerary')
  @ApiOperation({ summary: "Get a trip's itinerary, ordered by date then position" })
  @ApiOkResponse({ type: [ItineraryItemResponseDto] })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  getItinerary(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) tripId: string,
  ): Promise<ItineraryItemResponseDto[]> {
    return this.itineraryService.getItinerary(user.sub, tripId);
  }

  @Post('trips/:id/itinerary')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Add an itinerary item to a trip (appended to the end)' })
  @ApiOkResponse({ type: ItineraryItemResponseDto })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  createItineraryItem(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) tripId: string,
    @Body() dto: CreateItineraryItemDto,
  ): Promise<ItineraryItemResponseDto> {
    return this.itineraryService.createItem(user.sub, tripId, dto);
  }

  @Patch('trips/:id/reorder')
  @ApiOperation({ summary: "Reorder a trip's itinerary items" })
  @ApiOkResponse({ type: [ItineraryItemResponseDto] })
  @ApiNotFoundResponse({ description: "Trip does not exist or isn't the caller's." })
  reorder(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) tripId: string,
    @Body() dto: ReorderItineraryDto,
  ): Promise<ItineraryItemResponseDto[]> {
    return this.itineraryService.reorder(user.sub, tripId, dto);
  }

  @Get('dashboard')
  @ApiOperation({
    summary:
      "The caller's Travel dashboard: upcoming/active/completed trips, next destination, upcoming itinerary items, trip count",
  })
  @ApiOkResponse({ type: TravelDashboardResponseDto })
  getDashboard(@CurrentUser() user: JwtPayload): Promise<TravelDashboardResponseDto> {
    return this.travelService.getDashboard(user.sub);
  }
}
