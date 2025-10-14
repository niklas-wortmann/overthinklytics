'use client';

import * as React from 'react';
import { useEffect, useState } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  BarChart,
  Bar,
  AreaChart,
  Area,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import { Users, MousePointerClick, DollarSign, Percent } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Trend } from '@/components/ui/trend';
import { apiFetch } from '@/lib/apiClient';


const purple = '#8b5cf6';
const purpleAlt = '#a78bfa';
const grid = '#1f1f24';

export default function DashboardPage() {
  type Kpi = { label: string; value: string; delta: number };
  type TrafficPoint = { day: string; visits: number; sessions: number };
  type SignupPoint = { channel: string; signups: number };
  type RevenuePoint = { day: string; value: number };
  type DevicePoint = { name: string; value: number };

  const [kpis, setKpis] = useState<Kpi[]>([]);
  const [trafficData, setTrafficData] = useState<TrafficPoint[]>([]);
  const [signupsData, setSignupsData] = useState<SignupPoint[]>([]);
  const [revenueData, setRevenueData] = useState<RevenuePoint[]>([]);
  const [deviceShareData, setDeviceShareData] = useState<DevicePoint[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  function iconFor(label: string) {
    switch (label) {
      case 'Total Users':
        return <Users className="text-primary" size={18} />;
      case 'Sessions':
        return <MousePointerClick className="text-primary" size={18} />;
      case 'Conversion':
        return <Percent className="text-primary" size={18} />;
      case 'Revenue':
        return <DollarSign className="text-primary" size={18} />;
      default:
        return null;
    }
  }

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        const [kpiRes, trafficRes, signupRes, revenueRes, deviceRes] = await Promise.all([
          apiFetch<{ kpis: Kpi[] }>('/analytics/kpis'),
          apiFetch<{ data: TrafficPoint[] }>(`/analytics/traffic?limit=10`),
          apiFetch<{ data: SignupPoint[] }>(`/analytics/signups`),
          apiFetch<{ data: RevenuePoint[] }>(`/analytics/revenue?limit=10`),
          apiFetch<{ data: DevicePoint[] }>(`/analytics/device-share`),
        ]);
        if (cancelled) return;
        if (kpiRes.ok && kpiRes.data) setKpis(kpiRes.data.kpis);
        if (trafficRes.ok && trafficRes.data) setTrafficData(trafficRes.data.data);
        if (signupRes.ok && signupRes.data) setSignupsData(signupRes.data.data);
        if (revenueRes.ok && revenueRes.data) setRevenueData(revenueRes.data.data);
        if (deviceRes.ok && deviceRes.data) setDeviceShareData(deviceRes.data.data);
        if (!kpiRes.ok || !trafficRes.ok || !signupRes.ok || !revenueRes.ok || !deviceRes.ok) {
          setError(
            [kpiRes, trafficRes, signupRes, revenueRes, deviceRes].find((r) => !r.ok)?.error || 'Failed to load some data'
          );
        }
      } catch (e: any) {
        if (!cancelled) setError(e?.message || 'Unexpected error');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="mx-auto max-w-7xl px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl md:text-3xl font-bold tracking-tight">Analytics Overview</h1>
        <p className="text-muted mt-1">Live data loaded from the database via API.</p>
      </div>
      {error && (
              <div className="mb-4 text-sm text-red-400">{error}</div>
            )}
            {loading && !error && (
              <div className="mb-4 text-sm text-zinc-400">Loading analytics...</div>
            )}
            {/* KPI cards */}
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6">
        {kpis.map((k) => (
          <Card key={k.label}>
            <CardHeader className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                {iconFor(k.label)}
                {k.label}
              </CardTitle>
              <Trend value={k.delta} label={`${k.label} trend`} />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-semibold">{k.value}</div>
            </CardContent>
          </Card>
        ))}
      </section>

      {/* Charts grid */}
      <section className="grid gap-4 lg:grid-cols-3">
        {/* Line chart: Traffic */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Traffic (Visits vs Sessions)</CardTitle>
            <CardDescription>Last 10 days</CardDescription>
          </CardHeader>
          <CardContent className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trafficData} margin={{ left: 8, right: 8, top: 10, bottom: 0 }}>
                <CartesianGrid stroke={grid} strokeDasharray="3 3" />
                <XAxis dataKey="day" stroke="#888" tick={{ fill: '#a1a1aa', fontSize: 12 }} />
                <YAxis stroke="#888" tick={{ fill: '#a1a1aa', fontSize: 12 }} />
                <Tooltip
                  contentStyle={{ background: '#0b0b0e', border: `1px solid ${grid}`, color: '#fff' }}
                  labelStyle={{ color: '#a1a1aa' }}
                />
                <Line type="monotone" dataKey="visits" stroke={purple} strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="sessions" stroke={purpleAlt} strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Bar chart: Signups by Channel */}
        <Card>
          <CardHeader>
            <CardTitle>Signups by Channel</CardTitle>
            <CardDescription>Current month</CardDescription>
          </CardHeader>
          <CardContent className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={signupsData} margin={{ left: 8, right: 8, top: 10, bottom: 0 }}>
                <CartesianGrid stroke={grid} strokeDasharray="3 3" />
                <XAxis dataKey="channel" stroke="#888" tick={{ fill: '#a1a1aa', fontSize: 12 }} />
                <YAxis stroke="#888" tick={{ fill: '#a1a1aa', fontSize: 12 }} />
                <Tooltip contentStyle={{ background: '#0b0b0e', border: `1px solid ${grid}`, color: '#fff' }} />
                <Bar dataKey="signups" fill={purple} radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Area chart: Revenue */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Revenue</CardTitle>
            <CardDescription>Last 10 days</CardDescription>
          </CardHeader>
          <CardContent className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={revenueData} margin={{ left: 8, right: 8, top: 10, bottom: 0 }}>
                <defs>
                  <linearGradient id="rev" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor={purple} stopOpacity={0.7} />
                    <stop offset="95%" stopColor={purple} stopOpacity={0.05} />
                  </linearGradient>
                </defs>
                <CartesianGrid stroke={grid} strokeDasharray="3 3" />
                <XAxis dataKey="day" stroke="#888" tick={{ fill: '#a1a1aa', fontSize: 12 }} />
                <YAxis stroke="#888" tick={{ fill: '#a1a1aa', fontSize: 12 }} />
                <Tooltip contentStyle={{ background: '#0b0b0e', border: `1px solid ${grid}`, color: '#fff' }} />
                <Area type="monotone" dataKey="value" stroke={purple} fill="url(#rev)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Pie chart: Device Share */}
        <Card>
          <CardHeader>
            <CardTitle>Device Share</CardTitle>
            <CardDescription>Last 30 days</CardDescription>
          </CardHeader>
          <CardContent className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={deviceShareData} dataKey="value" nameKey="name" innerRadius={48} outerRadius={80}>
                  {deviceShareData.map((_, i) => (
                    <Cell key={i} fill={i === 0 ? purple : i === 1 ? purpleAlt : '#6b7280'} />
                  ))}
                </Pie>
                <Legend wrapperStyle={{ color: '#a1a1aa' }} />
                <Tooltip contentStyle={{ background: '#0b0b0e', border: `1px solid ${grid}`, color: '#fff' }} />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </section>
    </main>
  );
}
