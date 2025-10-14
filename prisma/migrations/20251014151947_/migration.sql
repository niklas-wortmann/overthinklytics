-- CreateTable
CREATE TABLE "KpiSnapshot" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "capturedAt" DATETIME NOT NULL,
    "totalUsers" INTEGER NOT NULL,
    "sessions" INTEGER NOT NULL,
    "conversionPct" REAL NOT NULL,
    "revenueCents" INTEGER NOT NULL
);

-- CreateTable
CREATE TABLE "TrafficDaily" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "date" DATETIME NOT NULL,
    "visits" INTEGER NOT NULL,
    "sessions" INTEGER NOT NULL
);

-- CreateTable
CREATE TABLE "SignupByChannel" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "year" INTEGER NOT NULL,
    "month" INTEGER NOT NULL,
    "channel" TEXT NOT NULL,
    "signups" INTEGER NOT NULL
);

-- CreateTable
CREATE TABLE "RevenueDaily" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "date" DATETIME NOT NULL,
    "valueCents" INTEGER NOT NULL
);

-- CreateTable
CREATE TABLE "DeviceShare" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "snapshotDate" DATETIME NOT NULL,
    "device" TEXT NOT NULL,
    "sharePct" REAL NOT NULL
);

-- CreateIndex
CREATE UNIQUE INDEX "KpiSnapshot_capturedAt_key" ON "KpiSnapshot"("capturedAt");

-- CreateIndex
CREATE UNIQUE INDEX "TrafficDaily_date_key" ON "TrafficDaily"("date");

-- CreateIndex
CREATE INDEX "TrafficDaily_date_idx" ON "TrafficDaily"("date");

-- CreateIndex
CREATE INDEX "SignupByChannel_year_month_idx" ON "SignupByChannel"("year", "month");

-- CreateIndex
CREATE UNIQUE INDEX "SignupByChannel_year_month_channel_key" ON "SignupByChannel"("year", "month", "channel");

-- CreateIndex
CREATE UNIQUE INDEX "RevenueDaily_date_key" ON "RevenueDaily"("date");

-- CreateIndex
CREATE INDEX "RevenueDaily_date_idx" ON "RevenueDaily"("date");

-- CreateIndex
CREATE INDEX "DeviceShare_snapshotDate_idx" ON "DeviceShare"("snapshotDate");

-- CreateIndex
CREATE UNIQUE INDEX "DeviceShare_snapshotDate_device_key" ON "DeviceShare"("snapshotDate", "device");
