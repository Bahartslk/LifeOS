import type { TaskStats, TripStats } from '../../shared/stats/types';
import { DistributionBar, ProgressBar } from '../../shared/ui/ProgressBar';

/** System-wide task charts, shown on the dashboard only. */
export function TaskCharts({ stats }: { stats: TaskStats }) {
  return (
    <div className="card chart-card">
      <ProgressBar label="Görev tamamlanma oranı" percent={stats.completionRate} tone="teal" />
      <DistributionBar
        label="Görev durumu dağılımı"
        emptyText="Henüz görev yok."
        segments={[
          { label: 'Yapılacak', value: stats.todo, tone: 'neutral' },
          { label: 'Devam eden', value: stats.inProgress, tone: 'violet' },
          { label: 'Tamamlanan', value: stats.done, tone: 'teal' },
        ]}
      />
    </div>
  );
}

/** System-wide trip chart, shown on the dashboard only. */
export function TripCharts({ stats }: { stats: TripStats }) {
  return (
    <div className="card chart-card">
      <DistributionBar
        label="Seyahat durumu dağılımı"
        emptyText="Henüz seyahat yok."
        segments={[
          { label: 'Planlanan', value: stats.planned, tone: 'violet' },
          { label: 'Devam eden', value: stats.ongoing, tone: 'teal' },
          { label: 'Tamamlanan', value: stats.completed, tone: 'neutral' },
          { label: 'İptal edilen', value: stats.cancelled, tone: 'danger' },
        ]}
      />
    </div>
  );
}
