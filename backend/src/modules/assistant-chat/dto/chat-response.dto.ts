import { ApiProperty } from '@nestjs/swagger';

/** `POST /ai/chat`'s response. */
export class ChatResponseDto {
  @ApiProperty({
    description:
      "The assistant's reply, in Turkish. A suggestion only; nothing was created or changed.",
  })
  reply!: string;

  @ApiProperty({
    minimum: 0,
    description: "Chat messages the user can still send today (in the user's own time zone).",
  })
  remainingMessages!: number;
}
