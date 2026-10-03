import { PlannerDashboardResponseDto } from '../../planner/dto/dashboard-response.dto';
import { TaskResponseDto } from '../../planner/dto/task-response.dto';
import { MAX_CONTEXT_TASKS_PER_SECTION } from '../constants/ai.constants';
import { formatPlannerContext } from '../templates/format-context.util';
import { toPlannerContext } from './context.mapper';

function taskDto(title: string, dueDate: string, status = 'TODO'): TaskResponseDto {
  return {
    id: title,
    title,
    description: null,
    dueDate,
    dueTime: null,
    priority: 'MEDIUM',
    category: 'PERSONAL',
    status,
    source: 'PLANNER',
    taskListId: null,
    createdAt: new Date('2026-09-01T10:00:00Z'),
    updatedAt: new Date('2026-09-01T10:00:00Z'),
  } as TaskResponseDto;
}

function dashboard(overrides: Partial<PlannerDashboardResponseDto>): PlannerDashboardResponseDto {
  return {
    overdueTasks: [],
    todayTasks: [],
    upcomingTasks: [],
    completedCount: 0,
    pendingCount: 0,
    progressPercentage: 0,
    highPriorityTasks: [],
    travelTasks: [],
    ...overrides,
  };
}

describe('toPlannerContext', () => {
  it('keeps overdue, today and upcoming as separate lists', () => {
    const context = toPlannerContext(
      dashboard({
        overdueTasks: [taskDto('late', '2026-09-30', 'IN_PROGRESS')],
        todayTasks: [taskDto('now', '2026-10-02')],
        upcomingTasks: [taskDto('later', '2026-10-05')],
      }),
    );

    expect(context.overdueTasks.map((t) => t.title)).toEqual(['late']);
    expect(context.todayTasks.map((t) => t.title)).toEqual(['now']);
    expect(context.upcomingTasks.map((t) => t.title)).toEqual(['later']);
    expect(context.overdueTasks[0]).toEqual({
      title: 'late',
      dueDate: '2026-09-30',
      dueTime: null,
      priority: 'MEDIUM',
      status: 'IN_PROGRESS',
    });
  });

  it('truncates overdue tasks like every other section', () => {
    const many = Array.from({ length: MAX_CONTEXT_TASKS_PER_SECTION + 3 }, (_, i) =>
      taskDto(`late-${i}`, '2026-09-01'),
    );

    expect(toPlannerContext(dashboard({ overdueTasks: many })).overdueTasks).toHaveLength(
      MAX_CONTEXT_TASKS_PER_SECTION,
    );
  });
});

describe('formatPlannerContext', () => {
  it('renders an overdue section before today and upcoming', () => {
    const text = formatPlannerContext(
      toPlannerContext(
        dashboard({
          overdueTasks: [taskDto('late', '2026-09-30')],
          todayTasks: [taskDto('now', '2026-10-02')],
        }),
      ),
    );

    expect(text).toContain(
      'Overdue tasks (unfinished, due before today):\n- [TODO/MEDIUM] late (2026-09-30)',
    );
    expect(text.indexOf('Overdue tasks')).toBeLessThan(text.indexOf("Today's tasks"));
    expect(text).toContain('Upcoming tasks:\n(none)');
  });

  it('shows (none) when nothing is overdue', () => {
    expect(formatPlannerContext(toPlannerContext(dashboard({})))).toContain(
      'Overdue tasks (unfinished, due before today):\n(none)',
    );
  });
});
