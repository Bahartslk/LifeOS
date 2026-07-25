import { AiCapabilityService } from '../ai/capabilities/ai-capability.service';
import {
  OPPORTUNITY_DETECTION,
  PROACTIVE_SUGGESTIONS,
  REMINDER_RECOMMENDATIONS,
  SMART_INSIGHTS,
} from './capabilities/proactive-assistant.capabilities';
import { ProactiveAssistantService } from './proactive-assistant.service';
import { SuggestionConfidence, SuggestionDto, SuggestionPriority } from './dto/suggestion.dto';

function fakeCapabilityService(): AiCapabilityService {
  return { run: jest.fn().mockResolvedValue({ result: 'ok' }) } as unknown as AiCapabilityService;
}

function suggestion(overrides: Partial<SuggestionDto> = {}): SuggestionDto {
  return {
    title: 'Do something',
    description: 'A description.',
    reason: 'A reason.',
    priority: SuggestionPriority.MEDIUM,
    confidence: SuggestionConfidence.MEDIUM,
    expiresAt: null,
    ...overrides,
  };
}

describe('ProactiveAssistantService', () => {
  it('runs the suggestions capability for getSuggestions', async () => {
    const capabilityService = fakeCapabilityService();
    const service = new ProactiveAssistantService(capabilityService);

    await service.getSuggestions('user-1', 'focus on travel');

    expect(capabilityService.run).toHaveBeenCalledWith(
      PROACTIVE_SUGGESTIONS,
      'user-1',
      'focus on travel',
    );
  });

  it('runs the opportunity-detection capability for getOpportunities', async () => {
    const capabilityService = fakeCapabilityService();
    const service = new ProactiveAssistantService(capabilityService);

    await service.getOpportunities('user-1');

    expect(capabilityService.run).toHaveBeenCalledWith(OPPORTUNITY_DETECTION, 'user-1', undefined);
  });

  it('runs the reminder-recommendations capability for getReminders', async () => {
    const capabilityService = fakeCapabilityService();
    const service = new ProactiveAssistantService(capabilityService);

    await service.getReminders('user-1');

    expect(capabilityService.run).toHaveBeenCalledWith(
      REMINDER_RECOMMENDATIONS,
      'user-1',
      undefined,
    );
  });

  it('runs the smart-insights capability for getInsights', async () => {
    const capabilityService = fakeCapabilityService();
    const service = new ProactiveAssistantService(capabilityService);

    await service.getInsights('user-1');

    expect(capabilityService.run).toHaveBeenCalledWith(SMART_INSIGHTS, 'user-1', undefined);
  });

  it('propagates a capability execution failure instead of swallowing it', async () => {
    const capabilityService = {
      run: jest.fn().mockRejectedValue(new Error('provider unavailable')),
    } as unknown as AiCapabilityService;
    const service = new ProactiveAssistantService(capabilityService);

    await expect(service.getSuggestions('user-1')).rejects.toThrow('provider unavailable');
  });

  it('prioritizes without ever calling AiCapabilityService', () => {
    const capabilityService = fakeCapabilityService();
    const service = new ProactiveAssistantService(capabilityService);

    const result = service.prioritize([suggestion({ title: 'A' }), suggestion({ title: 'a' })]);

    expect(result.suggestions).toHaveLength(1);
    expect(result.removedDuplicateCount).toBe(1);
    expect(capabilityService.run).not.toHaveBeenCalled();
  });
});
