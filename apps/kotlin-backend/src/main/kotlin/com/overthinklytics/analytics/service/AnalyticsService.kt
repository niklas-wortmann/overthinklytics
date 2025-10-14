package com.overthinklytics.analytics.service

import com.overthinklytics.analytics.api.model.*
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AnalyticsService(
    private val kpiRepository: com.overthinklytics.analytics.repository.KpiRepository,
    private val trafficRepository: com.overthinklytics.analytics.repository.TrafficRepository,
    private val revenueRepository: com.overthinklytics.analytics.repository.RevenueRepository,
    private val signupRepository: com.overthinklytics.analytics.repository.SignupRepository,
    private val deviceShareRepository: com.overthinklytics.analytics.repository.DeviceShareRepository,
) {

    fun getKpis(): KpiResponse {
        val latest = kpiRepository.findLatest()
        if (latest == null) {
            return KpiResponse(emptyList())
        }
        val kpis = listOf(
            Kpi(label = "Total Users", value = formatNumber(latest.totalUsers), delta = 0.0),
            Kpi(label = "Sessions", value = formatNumber(latest.sessions), delta = 0.0),
            Kpi(label = "Conversion", value = String.format("%.1f%%", latest.conversionPct), delta = 0.0),
            Kpi(label = "Revenue", value = formatCurrencyK(latest.revenueCents / 100.0), delta = 0.0),
        )
        return KpiResponse(kpis)
    }

    fun getTraffic(limit: Int): TrafficResponse {
        val rows = trafficRepository.findRecent(limit)
        val points = rows.map { r ->
            TrafficPoint(
                day = r.date.toString(),
                visits = r.visits,
                sessions = r.sessions,
            )
        }
        return TrafficResponse(points)
    }

    fun getSignupsByChannel(): SignupResponse {
        val rows = signupRepository.findLatestMonth()
        return SignupResponse(
            data = rows.map { SignupPoint(it.channel, it.signups) }
        )
    }

    fun getRevenue(limit: Int): RevenueResponse {
        val rows = revenueRepository.findRecent(limit)
        val points = rows.map { r ->
            RevenuePoint(
                day = r.date.toString(),
                value = r.valueCents / 100.0,
            )
        }
        return RevenueResponse(points)
    }

    fun getDeviceShare(): DeviceShareResponse {
        val rows = deviceShareRepository.findLatestSnapshot()
        return DeviceShareResponse(
            data = rows.map { DeviceSharePoint(name = it.device, value = it.sharePct) }
        )
    }

    private fun formatNumber(n: Int): String = String.format("%,d", n)

    // Accepts dollars, formats with k when >= 1000, matching example style
    private fun formatCurrencyK(dollars: Double): String {
        return if (dollars >= 1000.0) "$" + String.format("%.1fk", dollars / 1000.0) else "$" + String.format("%,.0f", dollars)
    }
}
