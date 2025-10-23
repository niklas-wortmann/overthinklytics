package com.overthinklytics.analytics.service

import com.overthinklytics.analytics.api.model.*
import com.overthinklytics.analytics.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class AnalyticsService(
  private val kpiRepo: KpiSnapshotRepository,
  private val trafficRepo: TrafficDailyRepository,
  private val revenueRepo: RevenueDailyRepository,
  private val signupRepo: SignupByChannelRepository,
  private val deviceShareRepo: DeviceShareRepository,
) {

    fun getKpis(): KpiResponse {
        val latest = kpiRepo.findTopByOrderByCapturedAtDesc()
            ?: return KpiResponse(emptyList())
        val kpis = listOf(
            Kpi(label = "Total Users", value = formatNumber(latest.totalUsers), delta = 0.0),
            Kpi(label = "Sessions", value = formatNumber(latest.sessions), delta = 0.0),
            Kpi(label = "Conversion", value = String.format("%.1f%%", latest.conversionPct), delta = 0.0),
            Kpi(label = "Revenue", value = formatCurrencyK(latest.revenueCents / 100.0), delta = 0.0),
        )
        return KpiResponse(kpis)
    }

    fun getTraffic(limit: Int): TrafficResponse {
        val page = trafficRepo.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date")))
        val rows = page.content.asReversed() // ascending for charts
        val points = rows.map { r ->
            TrafficPoint(
                day = r.date,
                visits = r.visits,
                sessions = r.sessions,
            )
        }
        return TrafficResponse(points)
    }

    fun getSignupsByChannel(): SignupResponse {
        val top = signupRepo.findTopByOrderByYearDescMonthDesc() ?: return SignupResponse(emptyList())
        val yr = top.year
        val mon = top.month
        val rows = signupRepo.findByYearAndMonthOrderByChannelAsc(yr, mon)
        return SignupResponse(
            data = rows.map { SignupPoint(it.channel, it.signups) }
        )
    }

    fun getRevenue(limit: Int): RevenueResponse {
        val page = revenueRepo.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date")))
        val rows = page.content.asReversed()
        val points = rows.map { r ->
            RevenuePoint(
                day = r.date,
                value = r.valueCents / 100.0,
            )
        }
        return RevenueResponse(points)
    }

    fun getDeviceShare(): DeviceShareResponse {
        val rows = deviceShareRepo.findAll()
        return DeviceShareResponse(
            data = rows.map { DeviceSharePoint(name = it.id.device, value = it.sharePct, os = it.os) }
        )
    }

    private fun formatNumber(n: Int): String = String.format("%,d", n)

    // Accepts dollars, formats with k when >= 1000, matching example style
    private fun formatCurrencyK(dollars: Double): String {
        return if (dollars >= 1000.0) "$" + String.format("%.1fk", dollars / 1000.0) else "$" + String.format("%,.0f", dollars)
    }
}
