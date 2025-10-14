import { NextResponse } from 'next/server';
import { prisma } from '@/server/db';

export async function GET(req: Request) {
  try {
    const url = new URL(req.url);
    const yearParam = url.searchParams.get('year');
    const monthParam = url.searchParams.get('month');

    let year: number | undefined = yearParam ? Number(yearParam) : undefined;
    let month: number | undefined = monthParam ? Number(monthParam) : undefined;

    if (!year || !month) {
      // get latest year+month present in the table
      const latest = await prisma.signupByChannel.findFirst({
        orderBy: [{ year: 'desc' }, { month: 'desc' }],
      });
      if (latest) {
        year = latest.year;
        month = latest.month;
      }
    }

    const rows = await prisma.signupByChannel.findMany({
      where: year && month ? { year, month } : undefined,
      orderBy: { channel: 'asc' },
    });

    const data = rows.map((r) => ({ channel: r.channel, signups: r.signups }));
    return NextResponse.json({ data, year, month }, { status: 200 });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || 'Failed to load signups' }, { status: 500 });
  }
}
