"""Serializers for analytics API responses.

These serializers format data from the models to match the API contract
expected by the frontend, including number formatting, currency conversion,
and date formatting.
"""

from rest_framework import serializers


class KpiResponseSerializer(serializers.Serializer):
    """Response for /analytics/kpis endpoint."""

    def to_representation(self, instance):
        """Convert KpiSnapshot model to formatted KPI items."""
        if not instance:
            return {"kpis": []}

        # Configuration for each KPI: (label, field, formatter)
        kpi_config = [
            ("Total Users", instance.totalusers, lambda v: f"{v:,}"),
            ("Sessions", instance.sessions, lambda v: f"{v:,}"),
            ("Conversion", instance.conversionpct, lambda v: f"{v}%"),
            ("Revenue", instance.revenuecents, self._format_currency),
        ]

        return {
            "kpis": [
                {"label": label, "value": formatter(value), "delta": 0.0}
                for label, value, formatter in kpi_config
            ]
        }

    @staticmethod
    def _format_currency(cents):
        """Format cents as currency with 'k' suffix if >= $1000."""
        dollars = cents / 100
        return f"${dollars / 1000:.1f}k" if dollars >= 1000 else f"${dollars:,.0f}"


class TrafficPointSerializer(serializers.Serializer):
    """Single traffic data point."""

    day = serializers.SerializerMethodField()
    visits = serializers.IntegerField()
    sessions = serializers.IntegerField()

    def get_day(self, obj):
        """Format datetime as ISO date string."""
        dt = obj.date_datetime
        return dt.strftime("%Y-%m-%d") if dt else None


class TrafficResponseSerializer(serializers.Serializer):
    """Response for /analytics/traffic endpoint."""

    data = TrafficPointSerializer(many=True)


class SignupPointSerializer(serializers.Serializer):
    """Single signup data point."""

    channel = serializers.CharField()
    signups = serializers.IntegerField()


class SignupResponseSerializer(serializers.Serializer):
    """Response for /analytics/signups endpoint."""

    data = SignupPointSerializer(many=True)


class RevenuePointSerializer(serializers.Serializer):
    """Single revenue data point."""

    day = serializers.SerializerMethodField()
    value = serializers.SerializerMethodField()

    def get_day(self, obj):
        """Format datetime as ISO date string."""
        dt = obj.date_datetime
        return dt.strftime("%Y-%m-%d") if dt else None

    def get_value(self, obj):
        """Convert cents to dollars (float)."""
        return obj.valuecents / 100.0


class RevenueResponseSerializer(serializers.Serializer):
    """Response for /analytics/revenue endpoint."""

    data = RevenuePointSerializer(many=True)


class DeviceSharePointSerializer(serializers.Serializer):
    """Single device share data point."""

    name = serializers.CharField(source="device")
    value = serializers.FloatField(source="sharepct")


class DeviceShareResponseSerializer(serializers.Serializer):
    """Response for /analytics/device-share endpoint."""

    data = DeviceSharePointSerializer(many=True)


class ErrorResponseSerializer(serializers.Serializer):
    """Error response for validation failures."""

    error = serializers.CharField()
