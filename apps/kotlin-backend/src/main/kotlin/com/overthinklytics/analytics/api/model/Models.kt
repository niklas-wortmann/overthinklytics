package com.overthinklytics.analytics.api.model

// DTOs matching OpenAPI components.schemas

data class Kpi(
    val label: String,
    val value: String,
    val delta: Double
)

data class KpiResponse(
    val kpis: List<Kpi>
)

data class TrafficPoint(
    val day: String, // ISO date YYYY-MM-DD
    val visits: Int,
    val sessions: Int
)

data class TrafficResponse(
    val data: List<TrafficPoint>
)

data class SignupPoint(
    val channel: String,
    val signups: Int
)

data class SignupResponse(
    val data: List<SignupPoint>
)

data class RevenuePoint(
    val day: String,
    val value: Double
)

data class RevenueResponse(
    val data: List<RevenuePoint>
)

data class DeviceSharePoint(
    val name: String,
    val value: Double,
    val os: String?
)

data class DeviceShareResponse(
    val data: List<DeviceSharePoint>
)

// Error schema

data class ErrorResponse(
    val error: String
)
