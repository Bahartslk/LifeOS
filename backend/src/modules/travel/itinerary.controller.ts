import {
  Body,
  Controller,
  Delete,
  HttpCode,
  HttpStatus,
  Param,
  ParseUUIDPipe,
  Patch,
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
import { ItineraryService } from './itinerary.service';
import { UpdateItineraryItemDto } from './dto/update-itinerary-item.dto';
import { ItineraryItemResponseDto } from './dto/itinerary-item-response.dto';

/**
 * `PATCH`/`DELETE /travel/itinerary/:id` — top-level, not nested under
 * `/travel/trips/:id`, per Sprint 16.0's explicit (asymmetric) API list;
 * ownership is verified through the item's own trip relation instead of a
 * path param (see `ItineraryItemsRepository`'s doc comment). A separate
 * controller (rather than folding these two routes into
 * `TravelController`) purely to keep the distinct base path
 * (`travel/itinerary` vs. `travel`/`travel/trips`) — both share
 * `ItineraryService`.
 */
@ApiTags('travel')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('travel/itinerary')
export class ItineraryController {
  constructor(private readonly itineraryService: ItineraryService) {}

  @Patch(':id')
  @ApiOperation({ summary: 'Update an itinerary item (partial)' })
  @ApiOkResponse({ type: ItineraryItemResponseDto })
  @ApiNotFoundResponse({ description: "Item does not exist or isn't the caller's." })
  update(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
    @Body() dto: UpdateItineraryItemDto,
  ): Promise<ItineraryItemResponseDto> {
    return this.itineraryService.updateItem(user.sub, id, dto);
  }

  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiOperation({
    summary: 'Delete an itinerary item (also deletes its linked Planner task, if any)',
  })
  @ApiNotFoundResponse({ description: "Item does not exist or isn't the caller's." })
  async delete(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<void> {
    await this.itineraryService.deleteItem(user.sub, id);
  }
}
