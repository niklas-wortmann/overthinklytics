"""API views for analytics endpoints.

These views handle incoming requests, validate parameters, query data
using model managers, and return serialized responses.
"""

from rest_framework.response import Response
from rest_framework.views import APIView

from .models import (
    DeviceShare,
    KpiSnapshot,
    RevenueDaily,
    SignupByChannel,
    TrafficDaily,
)
from .serializers import (
    DeviceShareResponseSerializer,
    ErrorResponseSerializer,
    KpiResponseSerializer,
    RevenueResponseSerializer,
    SignupResponseSerializer,
    TrafficResponseSerializer,
)


class KpisView(APIView):
    """GET /analytics/kpis - Latest KPI snapshot."""

    def get(self, request):
        snapshot = KpiSnapshot.objects.get_latest()
        serializer = KpiResponseSerializer(snapshot)
        return Response(serializer.data)


class TrafficView(APIView):
    """GET /analytics/traffic?limit=10 - Recent traffic data."""

    def get(self, request):
        # Validate limit parameter
        limit = request.query_params.get("limit", "10")
        try:
            limit = int(limit)
            if not 1 <= limit <= 60:
                raise ValueError
        except ValueError:
            error_serializer = ErrorResponseSerializer(
                {"error": "limit must be between 1 and 60"}
            )
            return Response(error_serializer.data, status=400)

        # Query and serialize data
        traffic_data = list(TrafficDaily.objects.get_recent(limit))
        serializer = TrafficResponseSerializer({"data": traffic_data})
        return Response(serializer.data)


class SignupsView(APIView):
    """GET /analytics/signups - Latest month's signup breakdown."""

    def get(self, request):
        signups = SignupByChannel.objects.get_latest_month()
        serializer = SignupResponseSerializer({"data": signups})
        return Response(serializer.data)


class RevenueView(APIView):
    """GET /analytics/revenue?limit=10 - Recent revenue data."""

    def get(self, request):
        # Validate limit parameter
        limit = request.query_params.get("limit", "10")
        try:
            limit = int(limit)
            if not 1 <= limit <= 60:
                raise ValueError
        except ValueError:
            error_serializer = ErrorResponseSerializer(
                {"error": "limit must be between 1 and 60"}
            )
            return Response(error_serializer.data, status=400)

        # Query and serialize data
        revenue_data = list(RevenueDaily.objects.get_recent(limit))
        serializer = RevenueResponseSerializer({"data": revenue_data})
        return Response(serializer.data)


class DeviceShareView(APIView):
    """GET /analytics/device-share - Latest device distribution."""

    def get(self, request):
        devices = DeviceShare.objects.get_latest_snapshot()
        serializer = DeviceShareResponseSerializer({"data": devices})
        return Response(serializer.data)
