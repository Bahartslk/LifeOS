/** The LifeOS wordmark with its violet gradient tile, as on the mobile splash and login screens. */
export function BrandMark({ compact = false }: { compact?: boolean }) {
  return (
    <span className={compact ? 'brand brand--compact' : 'brand'}>
      <span className="brand__tile" aria-hidden="true">
        L
      </span>
      <span className="brand__name">LifeOS</span>
    </span>
  );
}
