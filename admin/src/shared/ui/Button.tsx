import type { ButtonHTMLAttributes, ReactNode } from 'react';

type Variant = 'primary' | 'outlined' | 'text';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  /** Shows progress and blocks repeat clicks while an action runs. */
  busy?: boolean;
  children: ReactNode;
}

export function Button({
  variant = 'primary',
  busy = false,
  disabled,
  className,
  children,
  type = 'button',
  ...rest
}: ButtonProps) {
  return (
    <button
      type={type}
      className={['button', `button--${variant}`, className].filter(Boolean).join(' ')}
      disabled={disabled || busy}
      aria-busy={busy || undefined}
      {...rest}
    >
      {busy && <span className="spinner spinner--small" aria-hidden="true" />}
      {children}
    </button>
  );
}
