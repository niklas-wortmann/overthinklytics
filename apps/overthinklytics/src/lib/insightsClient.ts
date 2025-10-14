import { apiFetch } from '@/lib/apiClient';
import { getSelectedTenantIds } from '@/lib/tenantSelection';

export async function fetchTenants() {
  return apiFetch<{ id: string; name: string }[]>('/me/tenants', {
    headers: { 'X-Demo-User-Id': '11111111-1111-1111-1111-111111111111' },
  });
}

export async function fetchInsights(params?: { from?: string; to?: string }) {
  const tenantIds = getSelectedTenantIds();
  const q = new URLSearchParams();
  if (tenantIds.length) q.set('tenantIds', tenantIds.join(','));
  if (params?.from) q.set('from', params.from);
  if (params?.to) q.set('to', params.to);
  const qs = q.toString();
  return apiFetch<{ tenants: string[]; data: any[] }>(`/insights${qs ? `?${qs}` : ''}`,
    { headers: { 'X-Demo-User-Id': '11111111-1111-1111-1111-111111111111' } });
}

export async function fetchInsightsSummary(params?: { from?: string; to?: string }) {
  const tenantIds = getSelectedTenantIds();
  const q = new URLSearchParams();
  console.log({tenantIds, q})
  if (tenantIds.length) q.set('tenantIds', tenantIds.join(','));
  if (params?.from) q.set('from', params.from);
  if (params?.to) q.set('to', params.to);
  const qs = q.toString();
  return apiFetch<{ tenants: string[]; perTenant: { tenantId: string; _sum: { value: string } }[]; total: number }>(
    `/insights/summary${qs ? `?${qs}` : ''}`,
    { headers: { 'X-Demo-User-Id': '11111111-1111-1111-1111-111111111111' } }
  );
}
