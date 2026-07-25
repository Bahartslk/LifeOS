import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsBoolean, IsOptional } from 'class-validator';

/**
 * The known keys of `User.notificationPreferences` (a `Json` column —
 * see that field's doc comment in `prisma/schema.prisma` for why). One
 * boolean per this app's three feature domains today; a future sprint
 * adding a fourth key extends this class only — no migration needed,
 * since the column itself is already schema-flexible.
 */
export class NotificationPreferencesDto {
  @ApiPropertyOptional()
  @IsOptional()
  @IsBoolean()
  taskReminders?: boolean;

  @ApiPropertyOptional()
  @IsOptional()
  @IsBoolean()
  tripReminders?: boolean;

  @ApiPropertyOptional()
  @IsOptional()
  @IsBoolean()
  aiInsights?: boolean;
}
