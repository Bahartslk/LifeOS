import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Transform, Type } from 'class-transformer';
import {
  ArrayMaxSize,
  IsArray,
  IsIn,
  IsNotEmpty,
  IsOptional,
  IsString,
  MaxLength,
  ValidateNested,
} from 'class-validator';

export const CHAT_MESSAGE_MAX_LENGTH = 500;
export const CHAT_HISTORY_MAX_ITEMS = 8;
/** Earlier assistant replies are also in the history, so a turn may be longer than a new message. */
export const CHAT_HISTORY_TEXT_MAX_LENGTH = 2000;

const trim = ({ value }: { value: unknown }) => (typeof value === 'string' ? value.trim() : value);

export class ChatTurnDto {
  @ApiProperty({ enum: ['user', 'assistant'] })
  @IsIn(['user', 'assistant'])
  role!: 'user' | 'assistant';

  @ApiProperty({ maxLength: CHAT_HISTORY_TEXT_MAX_LENGTH })
  @Transform(trim)
  @IsString()
  @IsNotEmpty()
  @MaxLength(CHAT_HISTORY_TEXT_MAX_LENGTH)
  text!: string;
}

/** `POST /ai/chat`'s body. Stateless: the client sends the recent turns it wants the assistant to remember. */
export class ChatRequestDto {
  @ApiProperty({ maxLength: CHAT_MESSAGE_MAX_LENGTH, example: 'Bugün neye odaklanmalıyım?' })
  @Transform(trim)
  @IsString()
  @IsNotEmpty()
  @MaxLength(CHAT_MESSAGE_MAX_LENGTH)
  message!: string;

  @ApiPropertyOptional({
    type: [ChatTurnDto],
    maxItems: CHAT_HISTORY_MAX_ITEMS,
    description: `Earlier turns of this conversation, oldest first, at most ${CHAT_HISTORY_MAX_ITEMS}. Nothing is stored on the server.`,
  })
  @IsOptional()
  @IsArray()
  @ArrayMaxSize(CHAT_HISTORY_MAX_ITEMS)
  @ValidateNested({ each: true })
  @Type(() => ChatTurnDto)
  history?: ChatTurnDto[];
}
