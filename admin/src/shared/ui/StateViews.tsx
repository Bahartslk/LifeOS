import type { ReactNode } from 'react';
import { describeError } from '../../core/api/errors';
import { Button } from './Button';
import { Icon } from './Icon';
import type { IconName } from './Icon';

export function LoadingState({ label = 'Yükleniyor…' }: { label?: string }) {
  return (
    <div className="state" role="status" aria-live="polite">
      <span className="spinner" aria-hidden="true" />
      <p className="state__text">{label}</p>
    </div>
  );
}

interface MessageStateProps {
  icon: IconName;
  title: string;
  description?: string;
  action?: ReactNode;
}

function MessageState({ icon, title, description, action }: MessageStateProps) {
  return (
    <div className="state">
      <span className="state__icon">
        <Icon name={icon} size={28} />
      </span>
      <h2 className="state__title">{title}</h2>
      {description && <p className="state__text">{description}</p>}
      {action}
    </div>
  );
}

export function EmptyState({ title, description, action }: Omit<MessageStateProps, 'icon'>) {
  return <MessageState icon="inbox" title={title} description={description} action={action} />;
}

/** A failed request, with the reason in Turkish and a way to try again. */
export function ErrorState({
  error,
  title = 'Veriler yüklenemedi',
  onRetry,
}: {
  error: unknown;
  title?: string;
  onRetry?: () => void;
}) {
  return (
    <div role="alert">
      <MessageState
        icon="alert"
        title={title}
        description={describeError(error)}
        action={
          onRetry && (
            <Button variant="outlined" onClick={onRetry}>
              <Icon name="refresh" size={18} />
              Tekrar dene
            </Button>
          )
        }
      />
    </div>
  );
}
