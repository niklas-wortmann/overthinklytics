import { PrismaClient } from '@prisma/client';
const prisma = new PrismaClient();

async function ensureTables() {
  // Create tables if they don't exist (SQLite only). Keep in sync with prisma/schema.prisma
  // KpiSnapshot
  await prisma.$executeRawUnsafe(
    `CREATE TABLE IF NOT EXISTS KpiSnapshot (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      capturedAt TEXT NOT NULL UNIQUE,
      totalUsers INTEGER NOT NULL,
      sessions INTEGER NOT NULL,
      conversionPct REAL NOT NULL,
      revenueCents INTEGER NOT NULL
    )`
  );

  // TrafficDaily
  await prisma.$executeRawUnsafe(
    `CREATE TABLE IF NOT EXISTS TrafficDaily (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      date TEXT NOT NULL UNIQUE,
      visits INTEGER NOT NULL,
      sessions INTEGER NOT NULL
    )`
  );
  await prisma.$executeRawUnsafe(
    `CREATE INDEX IF NOT EXISTS idx_TrafficDaily_date ON TrafficDaily(date)`
  );

  // SignupByChannel
  await prisma.$executeRawUnsafe(
    `CREATE TABLE IF NOT EXISTS SignupByChannel (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      year INTEGER NOT NULL,
      month INTEGER NOT NULL,
      channel TEXT NOT NULL,
      signups INTEGER NOT NULL,
      CONSTRAINT uniq_month_channel UNIQUE (year, month, channel)
    )`
  );
  await prisma.$executeRawUnsafe(
    `CREATE INDEX IF NOT EXISTS idx_SignupByChannel_year_month ON SignupByChannel(year, month)`
  );

  // RevenueDaily
  await prisma.$executeRawUnsafe(
    `CREATE TABLE IF NOT EXISTS RevenueDaily (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      date TEXT NOT NULL UNIQUE,
      valueCents INTEGER NOT NULL
    )`
  );
  await prisma.$executeRawUnsafe(
    `CREATE INDEX IF NOT EXISTS idx_RevenueDaily_date ON RevenueDaily(date)`
  );

  // DeviceShare
  await prisma.$executeRawUnsafe(
    `CREATE TABLE IF NOT EXISTS DeviceShare (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      snapshotDate TEXT NOT NULL,
      device TEXT NOT NULL,
      sharePct REAL NOT NULL,
      CONSTRAINT uniq_snapshot_device UNIQUE (snapshotDate, device)
    )`
  );
  await prisma.$executeRawUnsafe(
    `CREATE INDEX IF NOT EXISTS idx_DeviceShare_snapshotDate ON DeviceShare(snapshotDate)`
  );
}

async function main() {
  await ensureTables();

  // Demo data mirrors the dashboard (October 2025)
  const traffic = [
    { day: '2025-10-01', visits: 1240, sessions: 980 },
    { day: '2025-10-02', visits: 1380, sessions: 1040 },
    { day: '2025-10-03', visits: 1510, sessions: 1125 },
    { day: '2025-10-04', visits: 1675, sessions: 1210 },
    { day: '2025-10-05', visits: 1420, sessions: 1102 },
    { day: '2025-10-06', visits: 1890, sessions: 1330 },
    { day: '2025-10-07', visits: 2015, sessions: 1422 },
    { day: '2025-10-08', visits: 1940, sessions: 1378 },
    { day: '2025-10-09', visits: 2088, sessions: 1499 },
    { day: '2025-10-10', visits: 2142, sessions: 1540 },
  ];

  const signupsByChannel = [
    { channel: 'Organic', signups: 540 },
    { channel: 'Paid', signups: 320 },
    { channel: 'Referral', signups: 210 },
    { channel: 'Social', signups: 160 },
  ];

  const revenue = [
    { day: '2025-10-01', value: 1400 },
    { day: '2025-10-03', value: 1800 },
    { day: '2025-10-05', value: 1650 },
    { day: '2025-10-07', value: 2200 },
    { day: '2025-10-09', value: 2450 },
  ];

  const deviceShare = [
    { name: 'Desktop', value: 62 },
    { name: 'Mobile', value: 30 },
    { name: 'Tablet', value: 8 },
  ];

  // Seed KPI snapshot (single snapshot corresponding to the dashboard header)
  await prisma.kpiSnapshot.upsert({
    where: { capturedAt: new Date('2025-10-10T00:00:00.000Z') },
    create: {
      capturedAt: new Date('2025-10-10T00:00:00.000Z'),
      totalUsers: 24310,
      sessions: 15125,
      conversionPct: 3.9,
      revenueCents: 12400 * 100 / 100, // $12.4k approximated as 1,240,000 cents
    },
    update: {
      totalUsers: 24310,
      sessions: 15125,
      conversionPct: 3.9,
      revenueCents: 1240000,
    },
  });

  // Seed TrafficDaily
  for (const t of traffic) {
    await prisma.trafficDaily.upsert({
      where: { date: new Date(`${t.day}T00:00:00.000Z`) },
      create: { date: new Date(`${t.day}T00:00:00.000Z`), visits: t.visits, sessions: t.sessions },
      update: { visits: t.visits, sessions: t.sessions },
    });
  }

  // Seed SignupByChannel for October 2025
  for (const s of signupsByChannel) {
    await prisma.signupByChannel.upsert({
      where: { uniq_month_channel: { year: 2025, month: 10, channel: s.channel } },
      create: { year: 2025, month: 10, channel: s.channel, signups: s.signups },
      update: { signups: s.signups },
    });
  }

  // Seed RevenueDaily (values given are in dollars)
  for (const r of revenue) {
    await prisma.revenueDaily.upsert({
      where: { date: new Date(`${r.day}T00:00:00.000Z`) },
      create: { date: new Date(`${r.day}T00:00:00.000Z`), valueCents: Math.round(r.value * 100) },
      update: { valueCents: Math.round(r.value * 100) },
    });
  }

  // Seed DeviceShare snapshot for October 10, 2025
  const snapshotDate = new Date('2025-10-10T00:00:00.000Z');
  for (const d of deviceShare) {
    await prisma.deviceShare.upsert({
      where: { uniq_snapshot_device: { snapshotDate, device: d.name } },
      create: { snapshotDate, device: d.name, sharePct: d.value },
      update: { sharePct: d.value },
    });
  }
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
