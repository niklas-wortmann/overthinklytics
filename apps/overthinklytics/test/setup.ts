import '@testing-library/jest-dom/vitest';

// Load .env files for tests from the Next app directory using @next/env
import { loadEnvConfig } from '@next/env';
import path from 'node:path';

// Idempotent: calling multiple times is safe
loadEnvConfig(path.resolve(__dirname, '..'));

// Minimal Next.js mocks for tests that import next/navigation or next/router
import { vi } from 'vitest';

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    prefetch: vi.fn(),
    back: vi.fn(),
  }),
  usePathname: () => '/',
  useSearchParams: () => new URLSearchParams(),
}));

vi.mock('next/headers', () => ({
  cookies: () => ({ get: vi.fn(), set: vi.fn(), delete: vi.fn() }),
  headers: () => new Map<string, string>(),
}));
