"""URL routing for analytics API endpoints."""

from django.urls import path

from .views import (
    DeviceShareView,
    KpisView,
    RevenueView,
    SignupsView,
    TrafficView,
)

urlpatterns = [
    path("kpis/", KpisView.as_view(), name="kpis"),
    path("traffic/", TrafficView.as_view(), name="traffic"),
    path("signups/", SignupsView.as_view(), name="signups"),
    path("revenue/", RevenueView.as_view(), name="revenue"),
    path("device-share/", DeviceShareView.as_view(), name="device-share"),
]
