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
import { PlannerService } from './planner.service';
import { CreateTaskDto } from './dto/create-task.dto';
import { UpdateTaskDto } from './dto/update-task.dto';
import { TaskQueryDto } from './dto/task-query.dto';
import { TaskResponseDto } from './dto/task-response.dto';
import { PaginatedTasksResponseDto } from './dto/paginated-tasks-response.dto';
import { PlannerDashboardResponseDto } from './dto/dashboard-response.dto';

/**
 * Thin per CLAUDE.md's "business logic stays in use cases, not
 * controllers" rule — every handler is parse-DTO, call `PlannerService`,
 * return. Guarded at the controller level (`@UseGuards(JwtAuthGuard)`) since
 * every route here requires authentication, per this sprint's explicit
 * "every endpoint must require authentication" rule — no route needs its
 * own `@UseGuards` repeated. `@CurrentUser()`'s `sub` claim is the only
 * source of `userId` anywhere in this controller; no route ever accepts a
 * user id from the client.
 */
@ApiTags('planner')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('planner')
export class PlannerController {
  constructor(private readonly plannerService: PlannerService) {}

  @Post('tasks')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create a task' })
  @ApiOkResponse({ type: TaskResponseDto })
  @ApiNotFoundResponse({
    description: "taskListId does not refer to one of the caller's own task lists.",
  })
  createTask(
    @CurrentUser() user: JwtPayload,
    @Body() dto: CreateTaskDto,
  ): Promise<TaskResponseDto> {
    return this.plannerService.createTask(user.sub, dto);
  }

  @Get('tasks')
  @ApiOperation({ summary: "List the caller's tasks (filtered, sorted, cursor-paginated)" })
  @ApiOkResponse({ type: PaginatedTasksResponseDto })
  getTasks(
    @CurrentUser() user: JwtPayload,
    @Query() query: TaskQueryDto,
  ): Promise<PaginatedTasksResponseDto> {
    return this.plannerService.getTasks(user.sub, query);
  }

  @Get('tasks/:id')
  @ApiOperation({ summary: 'Get one task' })
  @ApiOkResponse({ type: TaskResponseDto })
  @ApiNotFoundResponse({ description: "Task does not exist or isn't the caller's." })
  getTask(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<TaskResponseDto> {
    return this.plannerService.getTask(user.sub, id);
  }

  @Patch('tasks/:id')
  @ApiOperation({ summary: 'Update a task (partial)' })
  @ApiOkResponse({ type: TaskResponseDto })
  @ApiNotFoundResponse({
    description: "Task (or referenced taskListId) does not exist or isn't the caller's.",
  })
  updateTask(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
    @Body() dto: UpdateTaskDto,
  ): Promise<TaskResponseDto> {
    return this.plannerService.updateTask(user.sub, id, dto);
  }

  @Delete('tasks/:id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiOperation({ summary: 'Delete a task (soft delete)' })
  @ApiNotFoundResponse({ description: "Task does not exist or isn't the caller's." })
  async deleteTask(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<void> {
    await this.plannerService.deleteTask(user.sub, id);
  }

  @Patch('tasks/:id/complete')
  @ApiOperation({ summary: 'Mark a task complete' })
  @ApiOkResponse({ type: TaskResponseDto })
  @ApiNotFoundResponse({ description: "Task does not exist or isn't the caller's." })
  markComplete(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<TaskResponseDto> {
    return this.plannerService.markComplete(user.sub, id);
  }

  @Patch('tasks/:id/incomplete')
  @ApiOperation({ summary: 'Mark a task incomplete' })
  @ApiOkResponse({ type: TaskResponseDto })
  @ApiNotFoundResponse({ description: "Task does not exist or isn't the caller's." })
  markIncomplete(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
  ): Promise<TaskResponseDto> {
    return this.plannerService.markIncomplete(user.sub, id);
  }

  @Get('dashboard')
  @ApiOperation({
    summary:
      "The caller's Planner dashboard: today/upcoming tasks, completion stats, high-priority and travel tasks",
  })
  @ApiOkResponse({ type: PlannerDashboardResponseDto })
  getDashboard(@CurrentUser() user: JwtPayload): Promise<PlannerDashboardResponseDto> {
    return this.plannerService.getDashboard(user.sub);
  }
}
