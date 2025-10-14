import { NextResponse } from 'next/server';
import { prisma } from '@/server/db';

// Returns the latest KPI snapshot mapped for the dashboard
export async function GET() {
  try {
    const snap = await prisma.kpiSnapshot.findFirst({
      orderBy: { capturedAt: 'desc' },
    });
    if (!snap) {
      return NextResponse.json({ kpis: [] }, { status: 200 });
    }

    const formatNumber = (n: number) => n.toLocaleString('en-US');
    const formatCurrencyK = (cents: number) => {
      const dollars = cents / 100;
      if (dollars >= 1000) return `$${(dollars / 1000).toFixed(1)}k`;
      return `$${Math.round(dollars).toLocaleString('en-US')}`;
    };

    const kpis = [
      { label: 'Total Users', value: formatNumber(snap.totalUsers), delta: 0 },
      { label: 'Sessions', value: formatNumber(snap.sessions), delta: 0 },
      { label: 'Conversion', value: `${snap.conversionPct}%`, delta: 0 },
      { label: 'Revenue', value: formatCurrencyK(snap.revenueCents), delta: 0 },
    ];

    return NextResponse.json({ kpis }, { status: 200 });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || 'Failed to load KPIs' }, { status: 500 });
  }
}
