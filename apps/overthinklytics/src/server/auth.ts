import { headers } from 'next/headers';

export async function getUserIdFromRequest(): Promise<string | null> {
  // TODO: replace with real auth (NextAuth/JWT). For now use X-Demo-User-Id header.
  const h = await headers();
  const id = h.get('x-demo-user-id');
  return id && id.length > 0 ? id : null;
}
