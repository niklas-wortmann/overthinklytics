"""Tests for analytics API endpoints, models, and serializers."""

from datetime import datetime

from django.db import connections
from django.test import TestCase
from rest_framework.test import APITestCase

from .models import (
    DeviceShare,
    KpiSnapshot,
    RevenueDaily,
    SignupByChannel,
    TrafficDaily,
)
from .serializers import (
    DeviceShareResponseSerializer,
    KpiResponseSerializer,
    RevenuePointSerializer,
    TrafficPointSerializer,
)


class BaseTestCase(TestCase):
    """Base test case with common fixtures."""

    databases = ["analytics"]

    @classmethod
    def _create_schema(cls):
        """Create tables for unmanaged models."""
        with connections["analytics"].cursor() as cursor:
            # Create tables with Django's expected id column
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS KpiSnapshot (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    capturedAt INTEGER UNIQUE NOT NULL,
                    totalUsers INTEGER NOT NULL,
                    sessions INTEGER NOT NULL,
                    conversionPct REAL NOT NULL,
                    revenueCents INTEGER NOT NULL
                )
            """)
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS TrafficDaily (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date INTEGER UNIQUE NOT NULL,
                    visits INTEGER NOT NULL,
                    sessions INTEGER NOT NULL
                )
            """)
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS RevenueDaily (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date INTEGER UNIQUE NOT NULL,
                    valueCents INTEGER NOT NULL
                )
            """)
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS SignupByChannel (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    year INTEGER NOT NULL,
                    month INTEGER NOT NULL,
                    channel TEXT NOT NULL,
                    signups INTEGER NOT NULL,
                    UNIQUE (year, month, channel)
                )
            """)
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS DeviceShare (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    snapshotDate INTEGER NOT NULL,
                    device TEXT NOT NULL,
                    sharePct REAL NOT NULL,
                    UNIQUE (snapshotDate, device)
                )
            """)

    @classmethod
    def setUpTestData(cls):
        """Create test data once for the entire test class."""
        # Create schema for unmanaged models
        cls._create_schema()

        # KPI snapshots
        cls.kpi1 = KpiSnapshot.objects.create(
            capturedat=1704441600000,  # Jan 5, 2024
            totalusers=15234,
            sessions=45678,
            conversionpct=3.2,
            revenuecents=123456,
        )
        cls.kpi2 = KpiSnapshot.objects.create(
            capturedat=1704528000000,  # Jan 6, 2024
            totalusers=15500,
            sessions=46000,
            conversionpct=3.5,
            revenuecents=150000,
        )

        # Traffic data
        for i in range(15):
            TrafficDaily.objects.create(
                date=1704441600000 + (i * 86400000),  # Daily increments
                visits=1000 + i * 10,
                sessions=800 + i * 8,
            )

        # Revenue data
        for i in range(15):
            RevenueDaily.objects.create(
                date=1704441600000 + (i * 86400000),
                valuecents=50000 + i * 1000,
            )

        # Signup data (two months)
        channels = ["organic", "paid", "referral", "social"]
        for channel in channels:
            SignupByChannel.objects.create(
                year=2024, month=1, channel=channel, signups=100 * (ord(channel[0]) % 10)
            )
            SignupByChannel.objects.create(
                year=2023, month=12, channel=channel, signups=50
            )

        # Device share data (two snapshots)
        devices = [("desktop", 45.5), ("mobile", 40.2), ("tablet", 14.3)]
        for device, share in devices:
            DeviceShare.objects.create(
                snapshotdate=1704441600000, device=device, sharepct=share
            )
            DeviceShare.objects.create(
                snapshotdate=1704355200000, device=device, sharepct=share - 5
            )


# API Endpoint Tests


class HealthEndpointTest(APITestCase):
    """Tests for /health endpoint."""

    def test_health_check(self):
        """Health endpoint returns ok status."""
        response = self.client.get("/health/")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data, {"status": "ok", "backend": "django"})


class KpisEndpointTest(BaseTestCase, APITestCase):
    """Tests for /analytics/kpis endpoint."""

    def test_returns_latest_kpi(self):
        """Returns the most recent KPI snapshot."""
        response = self.client.get("/analytics/kpis/")
        self.assertEqual(response.status_code, 200)
        self.assertIn("kpis", response.data)
        self.assertEqual(len(response.data["kpis"]), 4)

    def test_kpi_structure(self):
        """Each KPI has label, value, and delta."""
        response = self.client.get("/analytics/kpis/")
        for kpi in response.data["kpis"]:
            self.assertIn("label", kpi)
            self.assertIn("value", kpi)
            self.assertIn("delta", kpi)

    def test_currency_formatting_small(self):
        """Currency < $1000 formatted as dollars."""
        kpi = KpiSnapshot.objects.create(
            capturedat=1704614400000, totalusers=100, sessions=200,
            conversionpct=1.0, revenuecents=50000  # $500
        )
        serializer = KpiResponseSerializer(kpi)
        revenue_kpi = [k for k in serializer.data["kpis"] if k["label"] == "Revenue"][0]
        self.assertEqual(revenue_kpi["value"], "$500")

    def test_currency_formatting_large(self):
        """Currency >= $1000 formatted with 'k' suffix."""
        response = self.client.get("/analytics/kpis/")
        revenue_kpi = [k for k in response.data["kpis"] if k["label"] == "Revenue"][0]
        self.assertIn("k", revenue_kpi["value"])

    def test_number_formatting(self):
        """Numbers formatted with commas."""
        response = self.client.get("/analytics/kpis/")
        users_kpi = [k for k in response.data["kpis"] if k["label"] == "Total Users"][0]
        self.assertIn(",", users_kpi["value"])


class TrafficEndpointTest(BaseTestCase, APITestCase):
    """Tests for /analytics/traffic endpoint."""

    def test_default_limit(self):
        """Returns 10 records by default."""
        response = self.client.get("/analytics/traffic/")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.data["data"]), 10)

    def test_custom_limit(self):
        """Respects custom limit parameter."""
        response = self.client.get("/analytics/traffic/?limit=5")
        self.assertEqual(len(response.data["data"]), 5)

    def test_limit_validation_too_high(self):
        """Rejects limit > 60."""
        response = self.client.get("/analytics/traffic/?limit=61")
        self.assertEqual(response.status_code, 400)
        self.assertIn("error", response.data)

    def test_limit_validation_too_low(self):
        """Rejects limit < 1."""
        response = self.client.get("/analytics/traffic/?limit=0")
        self.assertEqual(response.status_code, 400)

    def test_limit_validation_non_numeric(self):
        """Rejects non-numeric limit."""
        response = self.client.get("/analytics/traffic/?limit=abc")
        self.assertEqual(response.status_code, 400)

    def test_data_structure(self):
        """Each data point has day, visits, sessions."""
        response = self.client.get("/analytics/traffic/?limit=1")
        point = response.data["data"][0]
        self.assertIn("day", point)
        self.assertIn("visits", point)
        self.assertIn("sessions", point)

    def test_date_format(self):
        """Dates formatted as 'Jan 5' format."""
        response = self.client.get("/analytics/traffic/?limit=1")
        day = response.data["data"][0]["day"]
        self.assertRegex(day, r"^[A-Z][a-z]{2} \d{1,2}$")

    def test_ascending_order(self):
        """Data returned in ascending date order."""
        response = self.client.get("/analytics/traffic/?limit=5")
        visits = [point["visits"] for point in response.data["data"]]
        self.assertEqual(visits, sorted(visits))


class RevenueEndpointTest(BaseTestCase, APITestCase):
    """Tests for /analytics/revenue endpoint."""

    def test_default_limit(self):
        """Returns 10 records by default."""
        response = self.client.get("/analytics/revenue/")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.data["data"]), 10)

    def test_custom_limit(self):
        """Respects custom limit parameter."""
        response = self.client.get("/analytics/revenue/?limit=5")
        self.assertEqual(len(response.data["data"]), 5)

    def test_limit_validation(self):
        """Validates limit parameter."""
        response = self.client.get("/analytics/revenue/?limit=100")
        self.assertEqual(response.status_code, 400)

    def test_cents_to_dollars_conversion(self):
        """Converts cents to dollars with rounding."""
        response = self.client.get("/analytics/revenue/?limit=1")
        value = response.data["data"][0]["value"]
        self.assertIsInstance(value, int)

    def test_date_format(self):
        """Dates formatted as 'Jan 5' format."""
        response = self.client.get("/analytics/revenue/?limit=1")
        day = response.data["data"][0]["day"]
        self.assertRegex(day, r"^[A-Z][a-z]{2} \d{1,2}$")

    def test_ascending_order(self):
        """Data returned in ascending date order."""
        response = self.client.get("/analytics/revenue/?limit=5")
        values = [point["value"] for point in response.data["data"]]
        self.assertEqual(values, sorted(values))


class SignupsEndpointTest(BaseTestCase, APITestCase):
    """Tests for /analytics/signups endpoint."""

    def test_returns_latest_month(self):
        """Returns only the most recent month's data."""
        response = self.client.get("/analytics/signups/")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.data["data"]), 4)  # 4 channels

    def test_data_structure(self):
        """Each data point has channel and signups."""
        response = self.client.get("/analytics/signups/")
        point = response.data["data"][0]
        self.assertIn("channel", point)
        self.assertIn("signups", point)

    def test_channel_ordering(self):
        """Channels ordered alphabetically."""
        response = self.client.get("/analytics/signups/")
        channels = [point["channel"] for point in response.data["data"]]
        self.assertEqual(channels, sorted(channels))


class DeviceShareEndpointTest(BaseTestCase, APITestCase):
    """Tests for /analytics/device-share endpoint."""

    def test_returns_latest_snapshot(self):
        """Returns only the most recent snapshot."""
        response = self.client.get("/analytics/device-share/")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.data["data"]), 3)  # 3 devices

    def test_data_structure(self):
        """Each data point has name and value."""
        response = self.client.get("/analytics/device-share/")
        point = response.data["data"][0]
        self.assertIn("name", point)
        self.assertIn("value", point)

    def test_field_mapping(self):
        """Device mapped to name, sharepct mapped to value."""
        response = self.client.get("/analytics/device-share/")
        names = [point["name"] for point in response.data["data"]]
        self.assertIn("desktop", names)
        self.assertIn("mobile", names)

    def test_device_ordering(self):
        """Devices ordered alphabetically."""
        response = self.client.get("/analytics/device-share/")
        names = [point["name"] for point in response.data["data"]]
        self.assertEqual(names, sorted(names))


# Model Manager Tests


class KpiSnapshotManagerTest(BaseTestCase):
    """Tests for KpiSnapshotManager."""

    def test_get_latest_returns_most_recent(self):
        """get_latest() returns the most recent snapshot."""
        latest = KpiSnapshot.objects.get_latest()
        self.assertEqual(latest.totalusers, 15500)

    def test_get_latest_with_empty_db(self):
        """get_latest() returns None when no data."""
        KpiSnapshot.objects.all().delete()
        latest = KpiSnapshot.objects.get_latest()
        self.assertIsNone(latest)


class TrafficDailyManagerTest(BaseTestCase):
    """Tests for TrafficDailyManager."""

    def test_get_recent_limit(self):
        """get_recent() respects limit parameter."""
        recent = list(TrafficDaily.objects.get_recent(5))
        self.assertEqual(len(recent), 5)

    def test_get_recent_ascending_order(self):
        """get_recent() returns data in ascending order."""
        recent = list(TrafficDaily.objects.get_recent(5))
        visits = [t.visits for t in recent]
        self.assertEqual(visits, sorted(visits))

    def test_date_datetime_property(self):
        """date_datetime property converts timestamp correctly."""
        traffic = TrafficDaily.objects.get_recent(1).__iter__().__next__()
        self.assertIsInstance(traffic.date_datetime, datetime)


class RevenueDailyManagerTest(BaseTestCase):
    """Tests for RevenueDailyManager."""

    def test_get_recent_limit(self):
        """get_recent() respects limit parameter."""
        recent = list(RevenueDaily.objects.get_recent(3))
        self.assertEqual(len(recent), 3)

    def test_get_recent_ascending_order(self):
        """get_recent() returns data in ascending order."""
        recent = list(RevenueDaily.objects.get_recent(5))
        values = [r.valuecents for r in recent]
        self.assertEqual(values, sorted(values))

    def test_date_datetime_property(self):
        """date_datetime property converts timestamp correctly."""
        revenue = RevenueDaily.objects.get_recent(1).__iter__().__next__()
        self.assertIsInstance(revenue.date_datetime, datetime)


class SignupByChannelManagerTest(BaseTestCase):
    """Tests for SignupByChannelManager."""

    def test_get_latest_month_returns_correct_month(self):
        """get_latest_month() returns the most recent month."""
        latest = SignupByChannel.objects.get_latest_month()
        self.assertTrue(all(s.year == 2024 and s.month == 1 for s in latest))

    def test_get_latest_month_ordering(self):
        """get_latest_month() orders by channel."""
        latest = list(SignupByChannel.objects.get_latest_month())
        channels = [s.channel for s in latest]
        self.assertEqual(channels, sorted(channels))

    def test_get_latest_month_empty_db(self):
        """get_latest_month() returns empty queryset when no data."""
        SignupByChannel.objects.all().delete()
        latest = SignupByChannel.objects.get_latest_month()
        self.assertEqual(latest.count(), 0)


class DeviceShareManagerTest(BaseTestCase):
    """Tests for DeviceShareManager."""

    def test_get_latest_snapshot_returns_correct_snapshot(self):
        """get_latest_snapshot() returns the most recent snapshot."""
        latest = DeviceShare.objects.get_latest_snapshot()
        # All should have the same (latest) snapshotdate
        dates = set(d.snapshotdate for d in latest)
        self.assertEqual(len(dates), 1)

    def test_get_latest_snapshot_ordering(self):
        """get_latest_snapshot() orders by device."""
        latest = list(DeviceShare.objects.get_latest_snapshot())
        devices = [d.device for d in latest]
        self.assertEqual(devices, sorted(devices))

    def test_get_latest_snapshot_empty_db(self):
        """get_latest_snapshot() returns empty queryset when no data."""
        DeviceShare.objects.all().delete()
        latest = DeviceShare.objects.get_latest_snapshot()
        self.assertEqual(latest.count(), 0)


# Serializer Tests


class KpiResponseSerializerTest(BaseTestCase):
    """Tests for KpiResponseSerializer."""

    def test_formats_currency_under_1000(self):
        """Currency under $1000 formatted without 'k' suffix."""
        kpi = KpiSnapshot.objects.create(
            capturedat=1704700000000, totalusers=100, sessions=200,
            conversionpct=1.0, revenuecents=50000
        )
        serializer = KpiResponseSerializer(kpi)
        revenue = [k for k in serializer.data["kpis"] if k["label"] == "Revenue"][0]
        self.assertEqual(revenue["value"], "$500")

    def test_formats_currency_over_1000(self):
        """Currency over $1000 formatted with 'k' suffix."""
        kpi = KpiSnapshot.objects.create(
            capturedat=1704700000000, totalusers=100, sessions=200,
            conversionpct=1.0, revenuecents=250000
        )
        serializer = KpiResponseSerializer(kpi)
        revenue = [k for k in serializer.data["kpis"] if k["label"] == "Revenue"][0]
        self.assertEqual(revenue["value"], "$2.5k")

    def test_handles_none_instance(self):
        """Handles None instance gracefully."""
        serializer = KpiResponseSerializer(None)
        # Serializer returns empty dict when passed None
        self.assertEqual(serializer.data, {})


class TrafficPointSerializerTest(BaseTestCase):
    """Tests for TrafficPointSerializer."""

    def test_date_format(self):
        """Formats date as 'Jan 5' format."""
        traffic = TrafficDaily.objects.get_recent(1).__iter__().__next__()
        serializer = TrafficPointSerializer(traffic)
        self.assertRegex(serializer.data["day"], r"^[A-Z][a-z]{2} \d{1,2}$")


class RevenuePointSerializerTest(BaseTestCase):
    """Tests for RevenuePointSerializer."""

    def test_converts_cents_to_dollars(self):
        """Converts cents to rounded dollars."""
        revenue = RevenueDaily.objects.create(
            date=1704700000000, valuecents=12345
        )
        serializer = RevenuePointSerializer(revenue)
        self.assertEqual(serializer.data["value"], 123)

    def test_date_format(self):
        """Formats date as 'Jan 5' format."""
        revenue = RevenueDaily.objects.get_recent(1).__iter__().__next__()
        serializer = RevenuePointSerializer(revenue)
        self.assertRegex(serializer.data["day"], r"^[A-Z][a-z]{2} \d{1,2}$")


class DeviceShareResponseSerializerTest(BaseTestCase):
    """Tests for DeviceShareResponseSerializer."""

    def test_field_mapping(self):
        """Maps device to name and sharepct to value."""
        devices = DeviceShare.objects.get_latest_snapshot()
        serializer = DeviceShareResponseSerializer({"data": devices})
        point = serializer.data["data"][0]
        self.assertIn("name", point)
        self.assertIn("value", point)
        self.assertNotIn("device", point)
        self.assertNotIn("sharepct", point)
