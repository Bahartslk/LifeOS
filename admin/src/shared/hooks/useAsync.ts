import { useCallback, useEffect, useState } from 'react';

export type AsyncState<T> =
  | { status: 'loading'; data: undefined; error: undefined }
  | { status: 'success'; data: T; error: undefined }
  | { status: 'error'; data: undefined; error: unknown };

const LOADING = { status: 'loading', data: undefined, error: undefined } as const;

/**
 * Runs `load` on mount and whenever `key` changes, exposing the usual
 * loading / success / error states. A response that arrives after the key
 * changed (or after unmount) is ignored, so a slow earlier request can
 * never overwrite a newer one.
 */
export function useAsync<T>(
  load: () => Promise<T>,
  key: string,
): AsyncState<T> & { reload: () => void } {
  const [state, setState] = useState<AsyncState<T> & { key: string }>({ ...LOADING, key });
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let current = true;
    load().then(
      (data) => {
        if (current) setState({ status: 'success', data, error: undefined, key });
      },
      (error: unknown) => {
        if (current) setState({ status: 'error', data: undefined, error, key });
      },
    );
    return () => {
      current = false;
    };
    // `load` is intentionally not a dependency: callers pass a fresh closure
    // on every render, and `key` is what identifies the request.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key, attempt]);

  const reload = useCallback(() => {
    setState({ ...LOADING, key });
    setAttempt((value) => value + 1);
  }, [key]);

  // While the result on hand belongs to a previous key, report "loading".
  const visible: AsyncState<T> = state.key === key ? state : LOADING;
  return { ...visible, reload };
}
