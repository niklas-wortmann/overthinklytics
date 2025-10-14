export function getSelectedTenantIds(): string[] {
  if (typeof window === 'undefined') return [];
  try { return JSON.parse(localStorage.getItem('ol_tenants') || '[]'); } catch { return []; }
}
export function setSelectedTenantIds(ids: string[]) {
  if (typeof window !== 'undefined') localStorage.setItem('ol_tenants', JSON.stringify(ids));
}
