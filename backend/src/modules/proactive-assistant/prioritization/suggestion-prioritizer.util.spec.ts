import { SuggestionConfidence, SuggestionDto, SuggestionPriority } from '../dto/suggestion.dto';
import { prioritizeSuggestions } from './suggestion-prioritizer.util';

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

describe('prioritizeSuggestions', () => {
  it('returns an empty result for an empty input', () => {
    expect(prioritizeSuggestions([])).toEqual({
      suggestions: [],
      removedDuplicateCount: 0,
      removedContradictionCount: 0,
    });
  });

  it('ranks HIGH priority above MEDIUM above LOW, regardless of input order', () => {
    const low = suggestion({ title: 'Low task', priority: SuggestionPriority.LOW });
    const high = suggestion({ title: 'High task', priority: SuggestionPriority.HIGH });
    const medium = suggestion({ title: 'Medium task', priority: SuggestionPriority.MEDIUM });

    const result = prioritizeSuggestions([low, high, medium]);

    expect(result.suggestions.map((s) => s.title)).toEqual([
      'High task',
      'Medium task',
      'Low task',
    ]);
  });

  it('uses confidence to break ties between suggestions of equal priority', () => {
    const lowConfidence = suggestion({
      title: 'Less certain',
      priority: SuggestionPriority.HIGH,
      confidence: SuggestionConfidence.LOW,
    });
    const highConfidence = suggestion({
      title: 'More certain',
      priority: SuggestionPriority.HIGH,
      confidence: SuggestionConfidence.HIGH,
    });

    const result = prioritizeSuggestions([lowConfidence, highConfidence]);

    expect(result.suggestions.map((s) => s.title)).toEqual(['More certain', 'Less certain']);
  });

  it('breaks remaining ties by the soonest expiresAt', () => {
    const later = suggestion({ title: 'Later', expiresAt: '2026-09-01' });
    const sooner = suggestion({ title: 'Sooner', expiresAt: '2026-08-01' });

    const result = prioritizeSuggestions([later, sooner]);

    expect(result.suggestions.map((s) => s.title)).toEqual(['Sooner', 'Later']);
  });

  it('deduplicates suggestions with the same normalized title, keeping the highest-ranked one', () => {
    const highVersion = suggestion({
      title: '  Pack Luggage  ',
      priority: SuggestionPriority.HIGH,
    });
    const lowVersion = suggestion({ title: 'pack luggage', priority: SuggestionPriority.LOW });

    const result = prioritizeSuggestions([lowVersion, highVersion]);

    expect(result.suggestions).toHaveLength(1);
    expect(result.suggestions[0].priority).toBe(SuggestionPriority.HIGH);
    expect(result.removedDuplicateCount).toBe(1);
  });

  it('removes the lower-ranked half of a contradictory pair', () => {
    const leaveEarlier = suggestion({
      title: 'Leave earlier for the airport',
      priority: SuggestionPriority.HIGH,
    });
    const leaveLater = suggestion({
      title: 'It is fine to leave later today',
      priority: SuggestionPriority.LOW,
    });

    const result = prioritizeSuggestions([leaveLater, leaveEarlier]);

    expect(result.suggestions).toHaveLength(1);
    expect(result.suggestions[0].title).toBe('Leave earlier for the airport');
    expect(result.removedContradictionCount).toBe(1);
  });

  it('keeps unrelated, non-contradicting, non-duplicate suggestions untouched', () => {
    const a = suggestion({ title: 'Pack luggage' });
    const b = suggestion({ title: 'Finish the report' });

    const result = prioritizeSuggestions([a, b]);

    expect(result.suggestions).toHaveLength(2);
    expect(result.removedDuplicateCount).toBe(0);
    expect(result.removedContradictionCount).toBe(0);
  });
});
