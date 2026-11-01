/** The fields of a Google service account key that sending through FCM needs. */
export interface ServiceAccount {
  projectId: string;
  clientEmail: string;
  privateKey: string;
}

/**
 * Reads `FIREBASE_SERVICE_ACCOUNT_JSON`: the key file's JSON as-is, or
 * base64-encoded (easier to paste into a single-line variable). A `\n`
 * escaped into the private key (common when the JSON is flattened) is
 * turned back into a line break. Throws with a message that names the
 * problem but never echoes the value.
 */
export function parseServiceAccount(raw: string): ServiceAccount {
  const text = raw.trim().startsWith('{')
    ? raw
    : Buffer.from(raw.trim(), 'base64').toString('utf8');
  let parsed: unknown;
  try {
    parsed = JSON.parse(text);
  } catch {
    throw new Error('FIREBASE_SERVICE_ACCOUNT_JSON is not valid JSON (or base64-encoded JSON).');
  }
  const json = parsed as Record<string, unknown>;
  const field = (name: string): string => {
    const value = json[name];
    if (typeof value !== 'string' || value.length === 0) {
      throw new Error(`FIREBASE_SERVICE_ACCOUNT_JSON has no "${name}".`);
    }
    return value;
  };
  return {
    projectId: field('project_id'),
    clientEmail: field('client_email'),
    privateKey: field('private_key').replace(/\\n/g, '\n'),
  };
}
