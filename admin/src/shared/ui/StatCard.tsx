import { formatNumber } from '../format/format';

type Accent = 'violet' | 'teal' | 'danger' | 'neutral';

interface StatCardProps {
  label: string;
  /** A count, or an already formatted value such as a percentage. */
  value: number | string;
  hint?: string;
  accent?: Accent;
}

export function StatCard({ label, value, hint, accent = 'neutral' }: StatCardProps) {
  return (
    <div className={`stat-card stat-card--${accent}`}>
      <span className="stat-card__label">{label}</span>
      <span className="stat-card__value">
        {typeof value === 'number' ? formatNumber(value) : value}
      </span>
      {hint && <span className="stat-card__hint">{hint}</span>}
    </div>
  );
}
