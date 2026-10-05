import type { ReactNode } from 'react';
import { roleLabel } from '../format/format';

export type BadgeTone = 'violet' | 'teal' | 'neutral' | 'danger';

export function Badge({ tone, children }: { tone: BadgeTone; children: ReactNode }) {
  return <span className={`badge badge--${tone}`}>{children}</span>;
}

export function RoleBadge({ role }: { role: string }) {
  return <Badge tone={role === 'ADMIN' ? 'violet' : 'neutral'}>{roleLabel(role)}</Badge>;
}

/** Account status: active, or soft-deleted. */
export function StatusBadge({ isActive }: { isActive: boolean }) {
  return isActive ? <Badge tone="teal">Aktif</Badge> : <Badge tone="danger">Silinmiş hesap</Badge>;
}
