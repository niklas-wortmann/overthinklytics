"use client";

import React from 'react';
import { getSelectedBackend, getApiBaseUrl } from '@/lib/config';
import { apiFetch } from '@/lib/apiClient';
import { BackendSwitcher } from '../dev/BackendSwitcher';

const SHOW_SWITCHER = (process.env.NEXT_PUBLIC_SHOW_BACKEND_SWITCHER || '')
  .toString()
  .toLowerCase();
const shouldShowSwitcher = SHOW_SWITCHER === '1' || SHOW_SWITCHER === 'true';

function useHotkeyToggle(onToggle: () => void) {
  React.useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      // Alt + D toggles
      if (e.altKey && (e.key === 'd' || e.key === 'D')) {
        e.preventDefault();
        onToggle();
      }
      // ESC closes
      if (e.key === 'Escape') {
        onToggle();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onToggle]);
}

function StatusDot({ backend }: { backend: string }) {
  const color = backend.startsWith('django')
    ? '#10b981' // emerald
    : backend.startsWith('kotlin')
    ? '#6366f1' // indigo
    : backend.startsWith('third') || backend.toLowerCase().includes('next')
    ? '#9ca3af' // gray
    : '#f59e0b'; // amber for custom
  return (
    <span
      aria-hidden
      style={{
        display: 'inline-block',
        width: 10,
        height: 10,
        borderRadius: 999,
        background: color,
        boxShadow: '0 0 0 2px rgba(0,0,0,0.35)'
      }}
    />
  );
}

export function DebugInfoToggle() {
  const [open, setOpen] = React.useState(false);
  // Read backend-related values after mount to avoid any SSR/CSR drift
  const [backend, setBackend] = React.useState<string>('');
  const [base, setBase] = React.useState<string>('');
  React.useEffect(() => {
    setBackend(getSelectedBackend());
    setBase(getApiBaseUrl());
  }, []);
  useHotkeyToggle(() => setOpen((o) => !o));

  // Minimal inline ping, reusing apiFetch
  const [pinging, setPinging] = React.useState(false);
  const [result, setResult] = React.useState<string | null>(null);
  const ping = async () => {
    setPinging(true);
    const res = await apiFetch<any>('/health');
    if (res.ok) setResult(typeof res.data === 'string' ? res.data : JSON.stringify(res.data));
    else setResult(`Error (${res.status}): ${res.error}`);
    setPinging(false);
  };

  return (
    <div style={{ position: 'fixed', left: 12, bottom: 12, zIndex: 60 }}>
      {/* Collapsed pill */}
      {!open && (
        <button
          aria-label={`Open debug info (backend: ${backend})`}
          onClick={() => setOpen(true)}
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 8,
            padding: '8px 10px',
            borderRadius: 999,
            background: 'rgba(32,32,36,0.9)',
            color: '#fff',
            border: '1px solid #333',
            fontSize: 12,
            WebkitBackdropFilter: 'blur(4px)',
            backdropFilter: 'blur(4px)'
          }}
        >
          <StatusDot backend={backend} />
          <span style={{ opacity: 0.9 }}>Debug</span>
        </button>
      )}

      {/* Expanded panel */}
      {open && (
        <div
          role="dialog"
          aria-label="Debug information"
          aria-modal={false}
          style={{
            minWidth: 300,
            maxWidth: 380,
            padding: 12,
            borderRadius: 10,
            background: 'rgba(20,20,24,0.95)',
            color: '#fff',
            border: '1px solid #333',
            boxShadow: '0 10px 30px rgba(0,0,0,0.45)'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <StatusDot backend={backend} />
              <strong style={{ fontSize: 12, letterSpacing: 0.3 }}>Debug info</strong>
            </div>
            <button
              onClick={() => setOpen(false)}
              aria-label="Close debug panel"
              style={{ background: 'transparent', color: '#bbb', border: 'none', fontSize: 12 }}
            >
              Close
            </button>
          </div>

          <div style={{ marginTop: 10, fontSize: 12, lineHeight: 1.4 }}>
            <div>
              <span style={{ opacity: 0.8 }}>Backend:</span>{' '}
              <code style={{ background: '#111', border: '1px solid #222', padding: '2px 6px', borderRadius: 6 }}>{backend}</code>
            </div>
            <div style={{ marginTop: 6 }}>
              <span style={{ opacity: 0.8 }}>Base URL:</span>{' '}
              <code title={base} style={{ background: '#111', border: '1px solid #222', padding: '2px 6px', borderRadius: 6 }}>{base}</code>
            </div>
          </div>

          <div style={{ marginTop: 10, display: 'flex', alignItems: 'center', gap: 8 }}>
            <button
              onClick={ping}
              disabled={pinging}
              style={{
                fontSize: 12,
                background: '#3b82f6',
                color: '#fff',
                border: '1px solid #1d4ed8',
                padding: '6px 8px',
                borderRadius: 6,
                opacity: pinging ? 0.7 : 1
              }}
            >
              {pinging ? 'Pinging…' : 'Ping /health'}
            </button>
            {result && (
              <span style={{ fontSize: 12, color: '#d1d5db' }}>{result}</span>
            )}
          </div>

          {shouldShowSwitcher && (
            <div style={{ marginTop: 12 }}>
              <BackendSwitcher />
            </div>
          )}
          <div style={{ marginTop: 8, opacity: 0.7, fontSize: 11 }}>
            Tip: Press Alt+D to toggle. Press ESC to close.
          </div>
        </div>
      )}
    </div>
  );
}
