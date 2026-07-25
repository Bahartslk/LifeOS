import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';
import { BudgetEstimateDto } from './trip-draft-response.dto';

export class BudgetAnalysisResultDto {
  @ApiProperty({ type: BudgetEstimateDto })
  accommodation!: BudgetEstimateDto;

  @ApiProperty({ type: BudgetEstimateDto })
  transportation!: BudgetEstimateDto;

  @ApiProperty({ type: BudgetEstimateDto })
  food!: BudgetEstimateDto;

  @ApiProperty({ type: BudgetEstimateDto })
  activities!: BudgetEstimateDto;

  @ApiProperty({ type: BudgetEstimateDto })
  miscellaneous!: BudgetEstimateDto;

  @ApiProperty({ type: BudgetEstimateDto })
  totalEstimatedBudget!: BudgetEstimateDto;

  @ApiProperty({ enum: ['LOW', 'MEDIUM', 'HIGH'] })
  confidence!: 'LOW' | 'MEDIUM' | 'HIGH';
}

/** `POST /travel/ai/budget-analysis`'s response — an estimate only; never written back to `Trip`. */
export class BudgetAnalysisResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: BudgetAnalysisResultDto })
  result!: BudgetAnalysisResultDto;
}
