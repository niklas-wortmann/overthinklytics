"""Analytics models for Overthinklytics.

These models map to the Prisma database tables in prisma/dev.db.
They are read-only (managed=False) since Prisma manages the schema.
"""

from datetime import datetime

from django.db import models
from django.db.models import F
from django.db.models.functions import Cast


class KpiSnapshotManager(models.Manager):
    """Custom manager for KPI snapshots."""

    def get_queryset(self):
        """Override to cast capturedat field to BigInteger (Django SQLite datetime issue)."""
        return super().get_queryset().annotate(
            capturedat_int=Cast("capturedat", models.BigIntegerField())
        )

    def get_latest(self):
        """Get the most recent KPI snapshot."""
        return self.order_by("-capturedat_int").first()


class KpiSnapshot(models.Model):
    """Key performance indicator snapshot."""

    capturedat = models.BigIntegerField(db_column="capturedAt", unique=True)
    totalusers = models.IntegerField(db_column="totalUsers")
    sessions = models.IntegerField()
    conversionpct = models.FloatField(db_column="conversionPct")
    revenuecents = models.IntegerField(db_column="revenueCents")

    objects = KpiSnapshotManager()

    class Meta:
        managed = False
        db_table = "KpiSnapshot"


class TrafficDailyManager(models.Manager):
    """Custom manager for traffic data."""

    def get_queryset(self):
        """Override to cast date field to BigInteger (Django SQLite datetime issue)."""
        return super().get_queryset().annotate(
            date_int=Cast("date", models.BigIntegerField())
        )

    def get_recent(self, limit=10):
        """Get recent traffic data, ordered by date ascending."""
        results = list(self.order_by("-date_int")[:limit])
        return reversed(results)  # Return in ascending order


class TrafficDaily(models.Model):
    """Daily traffic data."""

    date = models.BigIntegerField(unique=True, db_column="date")
    visits = models.IntegerField()
    sessions = models.IntegerField()

    objects = TrafficDailyManager()

    @property
    def date_datetime(self):
        """Convert Unix timestamp (ms) to datetime."""
        # Use annotated date_int if available, otherwise fall back to date
        timestamp = getattr(self, 'date_int', self.date)
        if timestamp:
            return datetime.fromtimestamp(timestamp / 1000)
        return None

    class Meta:
        managed = False
        db_table = "TrafficDaily"


class SignupByChannelManager(models.Manager):
    """Custom manager for signup data."""

    def get_latest_month(self):
        """Get signups for the most recent month, ordered by channel."""
        latest = self.order_by("-year", "-month").values("year", "month").first()
        if not latest:
            return self.none()
        return self.filter(
            year=latest["year"], month=latest["month"]
        ).order_by("channel")


class SignupByChannel(models.Model):
    """Monthly signup data by channel."""

    year = models.IntegerField()
    month = models.IntegerField()
    channel = models.TextField()
    signups = models.IntegerField()

    objects = SignupByChannelManager()

    class Meta:
        managed = False
        db_table = "SignupByChannel"
        unique_together = (("year", "month", "channel"),)


class RevenueDailyManager(models.Manager):
    """Custom manager for revenue data."""

    def get_queryset(self):
        """Override to cast date field to BigInteger (Django SQLite datetime issue)."""
        return super().get_queryset().annotate(
            date_int=Cast("date", models.BigIntegerField())
        )

    def get_recent(self, limit=10):
        """Get recent revenue data, ordered by date ascending."""
        results = list(self.order_by("-date_int")[:limit])
        return reversed(results)  # Return in ascending order


class RevenueDaily(models.Model):
    """Daily revenue data."""

    date = models.BigIntegerField(unique=True, db_column="date")
    valuecents = models.IntegerField(db_column="valueCents")

    objects = RevenueDailyManager()

    @property
    def date_datetime(self):
        """Convert Unix timestamp (ms) to datetime."""
        # Use annotated date_int if available, otherwise fall back to date
        timestamp = getattr(self, 'date_int', self.date)
        if timestamp:
            return datetime.fromtimestamp(timestamp / 1000)
        return None

    class Meta:
        managed = False
        db_table = "RevenueDaily"


class DeviceShareManager(models.Manager):
    """Custom manager for device share data."""

    def get_queryset(self):
        """Override to cast snapshotdate field to BigInteger (Django SQLite datetime issue)."""
        return super().get_queryset().annotate(
            snapshotdate_int=Cast("snapshotdate", models.BigIntegerField())
        )

    def get_latest_snapshot(self):
        """Get device shares for the most recent snapshot, ordered by device."""
        latest = self.order_by("-snapshotdate_int").values("snapshotdate_int").first()
        if not latest:
            return self.none()
        return self.filter(snapshotdate_int=latest["snapshotdate_int"]).order_by("device")


class DeviceShare(models.Model):
    """Device share snapshot data."""

    snapshotdate = models.BigIntegerField(db_column="snapshotDate")
    device = models.TextField()
    sharepct = models.FloatField(db_column="sharePct")

    objects = DeviceShareManager()

    class Meta:
        managed = False
        db_table = "DeviceShare"
        unique_together = (("snapshotdate", "device"),)
