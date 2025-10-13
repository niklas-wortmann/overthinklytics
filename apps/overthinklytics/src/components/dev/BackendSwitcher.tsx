"use client";

import React from 'react';
import { setBackendOverride, clearBackendOverride, getSelectedBackend } from '@/lib/config';

const BACKENDS = [
  { key: 'django', label: 'Django' },
  { key: 'kotlin', label: 'Kotlin/Spring' },
  { key: 'third', label: 'Third' },
];

export function BackendSwitcher() {
  const [value, setValue] = React.useState<string>(getSelectedBackend());

  React.useEffect(() => {
    setValue(getSelectedBackend());
  }, []);

  const onChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const v = e.target.value;
    setValue(v);
    setBackendOverride(v);
    // Reload to apply to any server-side data fetching as well
    window.location.reload();
  };

  const onClear = () => {
    clearBackendOverride();
    window.location.reload();
  };

  return (
    <div style={{ position: 'fixed', bottom: 12, right: 12, zIndex: 50 }}>
      <div style={{ padding: 8, background: 'rgba(32,32,36,0.85)', border: '1px solid #333', borderRadius: 8, color: '#fff', display: 'flex', gap: 8, alignItems: 'center' }}>
        <span style={{ fontSize: 12, opacity: 0.85 }}>Backend</span>
        <select value={value} onChange={onChange} style={{ background: '#111', color: '#fff', border: '1px solid #444', borderRadius: 6, padding: '4px 8px' }}>
          {BACKENDS.map(b => (
            <option key={b.key} value={b.key}>{b.label}</option>
          ))}
        </select>
        <button onClick={onClear} style={{ fontSize: 12, color: '#ddd', background: '#222', border: '1px solid #444', padding: '4px 8px', borderRadius: 6 }}>Clear</button>
      </div>
    </div>
  );
}
