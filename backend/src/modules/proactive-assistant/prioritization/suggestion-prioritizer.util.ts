import { SuggestionConfidence, SuggestionDto, SuggestionPriority } from '../dto/suggestion.dto';

export interface PrioritizationResult {
  suggestions: SuggestionDto[];
  removedDuplicateCount: number;
  removedContradictionCount: number;
}

const PRIORITY_WEIGHT: Record<SuggestionPriority, number> = {
  [SuggestionPriority.HIGH]: 3,
  [SuggestionPriority.MEDIUM]: 2,
  [SuggestionPriority.LOW]: 1,
};

const CONFIDENCE_WEIGHT: Record<SuggestionConfidence, number> = {
  [SuggestionConfidence.HIGH]: 3,
  [SuggestionConfidence.MEDIUM]: 2,
  [SuggestionConfidence.LOW]: 1,
};

/** Priority dominates the ranking (weighted x2); confidence only breaks ties between suggestions of equal priority. */
function score(suggestion: SuggestionDto): number {
  return PRIORITY_WEIGHT[suggestion.priority] * 2 + CONFIDENCE_WEIGHT[suggestion.confidence];
}

function normalizeTitle(title: string): string {
  return title.trim().toLowerCase().replace(/\s+/g, ' ');
}

function textOf(suggestion: SuggestionDto): string {
  return `${suggestion.title} ${suggestion.description} ${suggestion.reason}`.toLowerCase();
}

/**
 * Small, explicit keyword pairs a suggestion can't simultaneously recommend
 * alongside another (e.g. "leave earlier" and "leave later" for the same
 * commitment). Deliberately a short, literal list rather than a semantic/NLP
 * approach — genuine natural-language contradiction detection is a future
 * sprint's work (see the module README's Future Enhancements); this ships a
 * real, if narrow, algorithm for the pairs a proactive assistant is actually
 * likely to generate.
 */
const CONTRADICTION_KEYWORD_PAIRS: ReadonlyArray<readonly [string, string]> = [
  ['leave earlier', 'leave later'],
  ['take a break', 'keep working'],
  ['relax', 'work harder'],
  ['finish now', 'finish later'],
];

function contradicts(a: SuggestionDto, b: SuggestionDto): boolean {
  const textA = textOf(a);
  const textB = textOf(b);
  return CONTRADICTION_KEYWORD_PAIRS.some(
    ([left, right]) =>
      (textA.includes(left) && textB.includes(right)) ||
      (textA.includes(right) && textB.includes(left)),
  );
}

/**
 * Ranks, deduplicates, and removes contradictory suggestions — a pure,
 * deterministic algorithm over caller-supplied data (typically the output of
 * `POST /proactive-assistant/suggestions`), never another AI call: ranking
 * and deduplication are well-defined enough not to need a model, and running
 * one anyway would cost latency and tokens for no benefit, per this sprint's
 * "no duplicated AI calls" performance rule.
 *
 * Order of operations: rank first (by priority, then confidence, then
 * soonest `expiresAt`, then title — for full determinism), then deduplicate
 * by normalized title (keeping the highest-ranked survivor of each group),
 * then drop the lower-ranked half of any contradictory pair. Both counters
 * report how many inputs were dropped and why, so a caller can tell
 * "nothing was suggested" apart from "several were suggested and filtered".
 */
export function prioritizeSuggestions(suggestions: SuggestionDto[]): PrioritizationResult {
  const ranked = [...suggestions].sort((a, b) => {
    const scoreDiff = score(b) - score(a);
    if (scoreDiff !== 0) return scoreDiff;
    const expiresDiff = (a.expiresAt ?? '9999-99-99').localeCompare(b.expiresAt ?? '9999-99-99');
    if (expiresDiff !== 0) return expiresDiff;
    return a.title.localeCompare(b.title);
  });

  const deduped: SuggestionDto[] = [];
  const seenTitles = new Set<string>();
  let removedDuplicateCount = 0;
  for (const suggestion of ranked) {
    const key = normalizeTitle(suggestion.title);
    if (seenTitles.has(key)) {
      removedDuplicateCount += 1;
      continue;
    }
    seenTitles.add(key);
    deduped.push(suggestion);
  }

  const dropped = new Set<number>();
  let removedContradictionCount = 0;
  for (let i = 0; i < deduped.length; i += 1) {
    if (dropped.has(i)) continue;
    for (let j = i + 1; j < deduped.length; j += 1) {
      if (dropped.has(j)) continue;
      if (contradicts(deduped[i], deduped[j])) {
        dropped.add(j); // deduped is already rank-ordered, so j is the lower-ranked half of the pair
        removedContradictionCount += 1;
      }
    }
  }

  const finalSuggestions = deduped.filter((_, index) => !dropped.has(index));

  return { suggestions: finalSuggestions, removedDuplicateCount, removedContradictionCount };
}
