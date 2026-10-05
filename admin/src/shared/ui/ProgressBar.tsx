import { formatNumber, formatPercent } from '../format/format';

type Tone = 'violet' | 'teal' | 'danger' | 'neutral';

/** A single 0–100 value, e.g. a completion rate. */
export function ProgressBar({
  label,
  percent,
  tone = 'teal',
}: {
  label: string;
  percent: number;
  tone?: Tone;
}) {
  const value = Math.min(100, Math.max(0, percent));
  return (
    <div className="progress">
      <div className="progress__header">
        <span className="progress__label">{label}</span>
        <span className="progress__value">{formatPercent(value)}</span>
      </div>
      <div
        className="progress__track"
        role="progressbar"
        aria-label={label}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={value}
      >
        <div className={`progress__fill progress__fill--${tone}`} style={{ width: `${value}%` }} />
      </div>
    </div>
  );
}

export interface DistributionSegment {
  label: string;
  value: number;
  tone: Tone;
}

/**
 * How a total splits into parts, as one stacked bar with a legend. Shares
 * are computed from the counts the API returned; nothing here is invented.
 */
export function DistributionBar({
  label,
  segments,
  emptyText,
}: {
  label: string;
  segments: DistributionSegment[];
  emptyText: string;
}) {
  const total = segments.reduce((sum, segment) => sum + segment.value, 0);
  const share = (value: number) => (total === 0 ? 0 : (value / total) * 100);

  return (
    <div className="distribution">
      <span className="progress__label">{label}</span>
      <div className="distribution__track" aria-hidden="true">
        {segments
          .filter((segment) => segment.value > 0)
          .map((segment) => (
            <div
              key={segment.label}
              className={`progress__fill progress__fill--${segment.tone}`}
              style={{ width: `${share(segment.value)}%` }}
            />
          ))}
      </div>
      {total === 0 ? (
        <p className="distribution__empty">{emptyText}</p>
      ) : (
        <ul className="distribution__legend">
          {segments.map((segment) => (
            <li key={segment.label} className="distribution__item">
              <span className={`distribution__dot progress__fill--${segment.tone}`} />
              <span>{segment.label}</span>
              <span className="distribution__count">
                {formatNumber(segment.value)} · {formatPercent(Math.round(share(segment.value)))}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
