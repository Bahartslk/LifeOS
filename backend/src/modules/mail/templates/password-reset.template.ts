import { MailMessage } from '../mail.service';

/** HTML-escapes text placed into the email body (the display name is user input). */
function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/** The password reset email (Turkish, like the app's UI). The only place its wording lives. */
export function buildPasswordResetEmail(params: {
  email: string;
  displayName: string;
  code: string;
  validMinutes: number;
}): MailMessage {
  const { email, displayName, code, validMinutes } = params;
  const greeting = displayName ? `Merhaba ${displayName},` : 'Merhaba,';
  const subject = `LifeOS şifre sıfırlama kodun: ${code}`;
  const text = [
    greeting,
    '',
    `LifeOS şifreni sıfırlamak için kodun: ${code}`,
    `Kod ${validMinutes} dakika geçerli ve yalnızca bir kez kullanılabilir.`,
    '',
    'Bu isteği sen yapmadıysan bu e-postayı yok sayabilirsin; şifren değişmez.',
  ].join('\n');
  const html = `<!doctype html>
<html lang="tr">
  <body style="font-family: Arial, sans-serif; color: #1c1b1f; line-height: 1.5;">
    <p>${escapeHtml(greeting)}</p>
    <p>LifeOS şifreni sıfırlamak için kodun:</p>
    <p style="font-size: 28px; font-weight: bold; letter-spacing: 6px;">${code}</p>
    <p>Kod ${validMinutes} dakika geçerli ve yalnızca bir kez kullanılabilir.</p>
    <p style="color: #605d66;">Bu isteği sen yapmadıysan bu e-postayı yok sayabilirsin; şifren değişmez.</p>
  </body>
</html>`;
  return { to: { email, name: displayName || undefined }, subject, text, html };
}
