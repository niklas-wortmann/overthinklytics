// Runtime backend selection utilities for the frontend
// Works in both browser and (limited) server environments

export type BackendKey = 'django' | 'kotlin' | 'third' | string;

const FALLBACKS: Record<string, string> = {
  django: 'http://localhost:8000',
  kotlin: 'http://localhost:8080',
  third: 'http://localhost:3001', // Change this if your 3rd backend uses a different port
};

function safeGet(name: string): string | undefined {
  try {
    // Next.js exposes env vars that start with NEXT_PUBLIC_ to the browser
    // On the server, process.env is available
    const v = (process as any)?.env?.[name];
    if (typeof v === 'string' && v.length > 0) return v;
  } catch {}
  return undefined;
}

function getQueryParam(name: string): string | undefined {
  if (typeof window === 'undefined') return undefined;
  const url = new URL(window.location.href);
  const v = url.searchParams.get(name);
  return v ?? undefined;
}

function getCookie(name: string): string | undefined {
  if (typeof document === 'undefined') return undefined;
  const m = document.cookie.match(new RegExp('(?:^|; )' + name.replace(/([.$?*|{}()\[\]\\\/\+^])/g, '\\$1') + '=([^;]*)'));
  return m ? decodeURIComponent(m[1]) : undefined;
}

function getLocalStorage(name: string): string | undefined {
  if (typeof window === 'undefined') return undefined;
  try {
    const v = window.localStorage.getItem(name);
    return v ?? undefined;
  } catch {
    return undefined;
  }
}

export function setBackendOverride(backend: BackendKey) {
  if (typeof document !== 'undefined') {
    document.cookie = `ol_backend=${encodeURIComponent(backend)}; path=/; max-age=${60 * 60 * 24 * 365}`;
  }
  if (typeof window !== 'undefined') {
    try { window.localStorage.setItem('ol_backend', backend); } catch {}
  }
}

export function clearBackendOverride() {
  if (typeof document !== 'undefined') {
    document.cookie = 'ol_backend=; path=/; Max-Age=0';
  }
  if (typeof window !== 'undefined') {
    try { window.localStorage.removeItem('ol_backend'); } catch {}
  }
}

export function getSelectedBackend(): BackendKey {
  // Priority: URL ?backend= -> cookie/localStorage -> env NEXT_PUBLIC_BACKEND -> default "django"
  return (
    getQueryParam('backend') ||
    getLocalStorage('ol_backend') ||
    getCookie('ol_backend') ||
    safeGet('NEXT_PUBLIC_BACKEND') ||
    'django'
  );
}

export function getApiBaseUrl(): string {
  // Highest priority: explicit base URL
  const explicit = safeGet('NEXT_PUBLIC_API_BASE_URL');
  if (explicit) return explicit.replace(/\/?$/, '');

  const backend = getSelectedBackend();
  // Support per-backend URL envs, e.g. NEXT_PUBLIC_DJANGO_URL
  const perBackendEnv = safeGet(`NEXT_PUBLIC_${backend.toUpperCase()}_URL`);
  if (perBackendEnv) return perBackendEnv.replace(/\/?$/, '');

  const fallback = FALLBACKS[backend] ?? FALLBACKS['django'];
  return fallback.replace(/\/?$/, '');
}

export function withBase(path: string): string {
  const base = getApiBaseUrl();
  if (!path) return base;
  return `${base}${path.startsWith('/') ? '' : '/'}${path}`;
}
