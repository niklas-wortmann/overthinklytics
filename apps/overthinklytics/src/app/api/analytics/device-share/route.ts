import { NextResponse } from 'next/server';
import { prisma } from '@/server/db';

export async function GET(req: Request) {
  try {
    const url = new URL(req.url);
    const dateParam = url.searchParams.get('date');

    let snapshotDate: Date | undefined = dateParam ? new Date(dateParam) : undefined;
    if (!snapshotDate || isNaN(snapshotDate.getTime())) {
      const latest = await prisma.deviceShare.findFirst({ orderBy: { snapshotDate: 'desc' } });
      if (latest) snapshotDate = latest.snapshotDate;
    }

    const rows = await prisma.deviceShare.findMany({
      where: snapshotDate ? { snapshotDate } : undefined,
      orderBy: { device: 'asc' },
    });

    const data = rows.map((r) => ({ name: r.device, value: r.sharePct, os: r.os }));
    return NextResponse.json({ data, snapshotDate: snapshotDate?.toISOString() }, { status: 200 });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || 'Failed to load device share' }, { status: 500 });
  }
}
