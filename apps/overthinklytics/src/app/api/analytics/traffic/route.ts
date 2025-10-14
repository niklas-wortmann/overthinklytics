import { NextResponse } from 'next/server';
import { prisma } from '@/server/db';

function formatDayLabel(d: Date) {
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export async function GET(req: Request) {
  try {
    const url = new URL(req.url);
    const limit = Number(url.searchParams.get('limit') || '10');

    const rows = await prisma.trafficDaily.findMany({
      orderBy: { date: 'desc' },
      take: limit,
    });

    const data = rows
      .map((r) => ({ day: formatDayLabel(r.date), visits: r.visits, sessions: r.sessions, _date: r.date }))
      .sort((a, b) => a._date.getTime() - b._date.getTime())
      .map(({ _date, ...rest }) => rest);

    return NextResponse.json({ data }, { status: 200 });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || 'Failed to load traffic' }, { status: 500 });
  }
}
