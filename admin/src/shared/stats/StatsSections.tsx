import type { ReactNode } from 'react';
import { formatPercent } from '../format/format';
import { StatCard } from '../ui/StatCard';
import type { TaskStats, TripStats } from './types';

/**
 * Task counts by status. `overdueHint` says which "today" the overdue
 * figure is measured against. `children` is rendered below the cards —
 * the dashboard puts its charts there; a user's detail passes nothing.
 */
export function TaskStatsSection({
  stats,
  overdueHint,
  children,
}: {
  stats: TaskStats;
  overdueHint: string;
  children?: ReactNode;
}) {
  return (
    <section className="section" aria-labelledby="task-stats-title">
      <h2 id="task-stats-title" className="section__title">
        Görevler
      </h2>
      <div className="stat-grid stat-grid--six">
        <StatCard label="Toplam görev" value={stats.total} accent="violet" />
        <StatCard label="Yapılacak" value={stats.todo} />
        <StatCard label="Devam eden" value={stats.inProgress} />
        <StatCard label="Tamamlanan" value={stats.done} accent="teal" />
        <StatCard label="Geciken" value={stats.overdue} accent="danger" hint={overdueHint} />
        <StatCard
          label="Tamamlanma oranı"
          value={formatPercent(stats.completionRate)}
          accent="teal"
          hint="Tamamlanan / toplam görev"
        />
      </div>
      {children}
    </section>
  );
}

export function TripStatsSection({ stats, children }: { stats: TripStats; children?: ReactNode }) {
  return (
    <section className="section" aria-labelledby="trip-stats-title">
      <h2 id="trip-stats-title" className="section__title">
        Seyahatler
      </h2>
      <div className="stat-grid">
        <StatCard label="Toplam seyahat" value={stats.total} accent="violet" />
        <StatCard label="Planlanan" value={stats.planned} />
        <StatCard label="Devam eden" value={stats.ongoing} accent="teal" />
        <StatCard label="Tamamlanan" value={stats.completed} accent="teal" />
        <StatCard label="İptal edilen" value={stats.cancelled} />
      </div>
      {children}
    </section>
  );
}
