"""Django admin configuration for Overthinklytics analytics models."""

from datetime import datetime

from django.contrib import admin

from .models import (
    DeviceShare,
    KpiSnapshot,
    RevenueDaily,
    SignupByChannel,
    TrafficDaily,
)


class ReadOnlyAdminMixin:
    """Mixin to make admin read-only (for Prisma-managed models)."""

    def has_add_permission(self, request):
        return False

    def has_delete_permission(self, request, obj=None):
        return False


def format_timestamp_ms(timestamp_ms, fmt="%Y-%m-%d"):
    """Convert Unix timestamp in milliseconds to formatted string."""
    if timestamp_ms:
        return datetime.fromtimestamp(timestamp_ms / 1000).strftime(fmt)
    return None


def format_percentage(value):
    """Format a number as a percentage."""
    return f"{value:.2f}%"


def format_cents_as_dollars(cents):
    """Convert cents to dollar string."""
    return f"${cents / 100:.2f}"


@admin.register(KpiSnapshot)
class KpiSnapshotAdmin(ReadOnlyAdminMixin, admin.ModelAdmin):
    """Admin for KPI snapshots."""

    list_display = (
        "capturedat_display",
        "totalusers",
        "sessions",
        "conversionpct_display",
        "revenue_display",
    )
    list_filter = ("capturedat",)
    ordering = ("-capturedat",)

    @admin.display(description="Captured At", ordering="capturedat")
    def capturedat_display(self, obj):
        return format_timestamp_ms(obj.capturedat, "%Y-%m-%d %H:%M:%S")

    @admin.display(description="Conversion %", ordering="conversionpct")
    def conversionpct_display(self, obj):
        return format_percentage(obj.conversionpct)

    @admin.display(description="Revenue", ordering="revenuecents")
    def revenue_display(self, obj):
        return format_cents_as_dollars(obj.revenuecents)


@admin.register(TrafficDaily)
class TrafficDailyAdmin(ReadOnlyAdminMixin, admin.ModelAdmin):
    """Admin for daily traffic data."""

    list_display = ("date_display", "visits", "sessions")
    list_filter = ("date",)
    ordering = ("-date",)

    @admin.display(description="Date", ordering="date")
    def date_display(self, obj):
        return format_timestamp_ms(obj.date)


@admin.register(SignupByChannel)
class SignupByChannelAdmin(ReadOnlyAdminMixin, admin.ModelAdmin):
    """Admin for signup by channel data."""

    list_display = ("period_display", "channel", "signups")
    list_filter = ("year", "month", "channel")
    ordering = ("-year", "-month", "channel")
    search_fields = ("channel",)

    @admin.display(description="Period", ordering="year")
    def period_display(self, obj):
        return f"{obj.year}-{obj.month:02d}"


@admin.register(RevenueDaily)
class RevenueDailyAdmin(ReadOnlyAdminMixin, admin.ModelAdmin):
    """Admin for daily revenue data."""

    list_display = ("date_display", "revenue_display")
    list_filter = ("date",)
    ordering = ("-date",)

    @admin.display(description="Date", ordering="date")
    def date_display(self, obj):
        return format_timestamp_ms(obj.date)

    @admin.display(description="Revenue", ordering="valuecents")
    def revenue_display(self, obj):
        return format_cents_as_dollars(obj.valuecents)


@admin.register(DeviceShare)
class DeviceShareAdmin(ReadOnlyAdminMixin, admin.ModelAdmin):
    """Admin for device share data."""

    list_display = ("snapshotdate_display", "device", "sharepct_display")
    list_filter = ("snapshotdate", "device")
    ordering = ("-snapshotdate", "device")
    search_fields = ("device",)

    @admin.display(description="Snapshot Date", ordering="snapshotdate")
    def snapshotdate_display(self, obj):
        return format_timestamp_ms(obj.snapshotdate)

    @admin.display(description="Share %", ordering="sharepct")
    def sharepct_display(self, obj):
        return format_percentage(obj.sharepct)
