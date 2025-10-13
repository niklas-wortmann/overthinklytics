'use client';

import * as React from 'react';
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

// Dummy hard-coded data for October 2025
const traffic = [
  { day: 'Oct 1', visits: 1240, sessions: 980 },
  { day: 'Oct 2', visits: 1380, sessions: 1040 },
  { day: 'Oct 3', visits: 1510, sessions: 1125 },
  { day: 'Oct 4', visits: 1675, sessions: 1210 },
  { day: 'Oct 5', visits: 1420, sessions: 1102 },
  { day: 'Oct 6', visits: 1890, sessions: 1330 },
  { day: 'Oct 7', visits: 2015, sessions: 1422 },
  { day: 'Oct 8', visits: 1940, sessions: 1378 },
  { day: 'Oct 9', visits: 2088, sessions: 1499 },
  { day: 'Oct 10', visits: 2142, sessions: 1540 },
];

const signupsByChannel = [
  { channel: 'Organic', signups: 540 },
  { channel: 'Paid', signups: 320 },
  { channel: 'Referral', signups: 210 },
  { channel: 'Social', signups: 160 },
];

const revenue = [
  { day: 'Oct 1', value: 1400 },
  { day: 'Oct 3', value: 1800 },
  { day: 'Oct 5', value: 1650 },
  { day: 'Oct 7', value: 2200 },
  { day: 'Oct 9', value: 2450 },
];

const deviceShare = [
  { name: 'Desktop', value: 62 },
  { name: 'Mobile', value: 30 },
  { name: 'Tablet', value: 8 },
];

const purple = '#8b5cf6';
const purpleAlt = '#a78bfa';
const grid = '#1f1f24';

export default function DashboardPage() {
  // KPIs (hard-coded)
  const kpis = [
    {
      icon: <Users className="text-primary" size={18} />,
      label: 'Total Users',
      value: '24,310',
      delta: 5.4,
    },
    {
      icon: <MousePointerClick className="text-primary" size={18} />,
      label: 'Sessions',
      value: '15,125',
      delta: 3.1,
    },
    {
      icon: <Percent className="text-primary" size={18} />,
      label: 'Conversion',
      value: '3.9%',
      delta: -0.4,
    },
    {
      icon: <DollarSign className="text-primary" size={18} />,
      label: 'Revenue',
      value: '$12.4k',
      delta: 8.2,
    },
  ];

  return (
    <main className="mx-auto max-w-7xl px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl md:text-3xl font-bold tracking-tight">Analytics Overview</h1>
        <p className="text-muted mt-1">Dummy data for visualization — replace with real metrics later.</p>
      </div>
      {/* KPI cards */}
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6">
        {kpis.map((k) => (
          <Card key={k.label}>
            <CardHeader className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                {k.icon}
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
              <LineChart data={traffic} margin={{ left: 8, right: 8, top: 10, bottom: 0 }}>
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
              <BarChart data={signupsByChannel} margin={{ left: 8, right: 8, top: 10, bottom: 0 }}>
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
              <AreaChart data={revenue} margin={{ left: 8, right: 8, top: 10, bottom: 0 }}>
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
                <Pie data={deviceShare} dataKey="value" nameKey="name" innerRadius={48} outerRadius={80}>
                  {deviceShare.map((_, i) => (
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
