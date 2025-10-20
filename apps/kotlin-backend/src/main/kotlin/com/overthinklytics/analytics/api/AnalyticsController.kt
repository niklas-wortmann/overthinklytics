package com.overthinklytics.analytics.api

import com.overthinklytics.analytics.api.model.*
import com.overthinklytics.analytics.service.AnalyticsService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    @GetMapping("/kpis")
    fun getKpis(): ResponseEntity<KpiResponse> = ResponseEntity.ok(analyticsService.getKpis())

    @GetMapping("/traffic")
    fun getTraffic(
        @RequestParam(name = "limit", required = false, defaultValue = "10")
        @Min(1) @Max(60) limit: Int
    ): ResponseEntity<TrafficResponse> = ResponseEntity.ok(analyticsService.getTraffic(limit))

    @GetMapping("/signups")
    fun getSignupsByChannel(): ResponseEntity<SignupResponse> = ResponseEntity.ok(analyticsService.getSignupsByChannel())

    @GetMapping("/revenue")
    fun getRevenue(
        @RequestParam(name = "limit", required = false, defaultValue = "10")
        @Min(1) @Max(60) limit: Int
    ): ResponseEntity<RevenueResponse> = ResponseEntity.ok(analyticsService.getRevenue(limit))

    @GetMapping("/device-share")
    fun getDeviceShare(): ResponseEntity<DeviceShareResponse> = ResponseEntity.ok(analyticsService.getDeviceShare())
}
