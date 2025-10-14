'use client';
import React from 'react';
import { getSelectedTenantIds, setSelectedTenantIds } from '@/lib/tenantSelection';

type Tenant = { id: string; name: string };

export function TenantPicker({ allTenants }: { allTenants: Tenant[] }) {
  const [selected, setSelected] = React.useState<string[]>(getSelectedTenantIds());
  const toggle = (id: string) => {
    const next = selected.includes(id) ? selected.filter(x => x !== id) : [...selected, id];
    setSelected(next); setSelectedTenantIds(next);
  };
  const clear = () => { setSelected([]); setSelectedTenantIds([]); };

  return (
    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
      <button onClick={clear}>All my tenants</button>
      {allTenants.map(t => (
        <label key={t.id} style={{ border: '1px solid #444', padding: 6, borderRadius: 6 }}>
          <input type="checkbox" checked={selected.includes(t.id)} onChange={() => toggle(t.id)} /> {t.name}
        </label>
      ))}
    </div>
  );
}
