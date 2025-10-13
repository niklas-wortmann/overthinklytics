import { withBase } from './config';

export type ApiResult<T> = {
  ok: boolean;
  status: number;
  data?: T;
  error?: string;
};

export async function apiFetch<T = unknown>(path: string, init?: RequestInit): Promise<ApiResult<T>> {
  try {
    const res = await fetch(withBase(path), {
      ...init,
      headers: {
        'Content-Type': 'application/json',
        ...(init?.headers || {}),
      },
    });

    const contentType = res.headers.get('content-type') || '';
    const isJson = contentType.includes('application/json');
    const payload = isJson ? await res.json() : await res.text();

    if (!res.ok) {
      return { ok: false, status: res.status, error: typeof payload === 'string' ? payload : JSON.stringify(payload) };
    }

    return { ok: true, status: res.status, data: payload as T };
  } catch (e: any) {
    return { ok: false, status: 0, error: e?.message || 'Network error' };
  }
}
