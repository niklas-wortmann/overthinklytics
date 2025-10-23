// File: AnalyticsServiceTest.kt
package com.overthinklytics.analytics.service

import com.overthinklytics.analytics.entity.TrafficDailyEntity
import com.overthinklytics.analytics.repository.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@ExtendWith(MockitoExtension::class)
class AnalyticsServiceTest {

    @Mock
    private lateinit var kpiRepo: KpiSnapshotRepository

    @Mock
    private lateinit var trafficRepo: TrafficDailyRepository

    @Mock
    private lateinit var revenueRepo: RevenueDailyRepository

    @Mock
    private lateinit var signupRepo: SignupByChannelRepository

    @Mock
    private lateinit var deviceShareRepo: DeviceShareRepository


    @InjectMocks
    private lateinit var analyticsService: AnalyticsService

    /**
     * Tests the [AnalyticsService.getTraffic] method.
     * Verifies that it retrieves and sorts traffic data correctly.
     */
    @Test
    fun `should return traffic data sorted by ascending date`() {
        // Arrange
        val limit = 3
        val pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date"))

        val trafficEntity1 = TrafficDailyEntity().apply {
            date = "2025-01-13"
            visits = 100
            sessions = 50
        }
        val trafficEntity2 = TrafficDailyEntity().apply {
            date = "2025-01-14"
            visits = 200
            sessions = 100
        }
        val trafficEntity3 = TrafficDailyEntity().apply {
            date = "2025-01-15"
            visits = 300
            sessions = 150
        }

        val trafficData = listOf(trafficEntity3, trafficEntity2, trafficEntity1)
        val page = PageImpl(trafficData)

        Mockito.`when`(trafficRepo.findAll(pageRequest)).thenReturn(page)

        // Act
        val response = analyticsService.getTraffic(limit)

        // Assert
        assertEquals(3, response.data.size)
        assertEquals("2025-01-13", response.data[0].day)
        assertEquals(100, response.data[0].visits)
        assertEquals(50, response.data[0].sessions)

        assertEquals("2025-01-14", response.data[1].day)
        assertEquals(200, response.data[1].visits)
        assertEquals(100, response.data[1].sessions)

        assertEquals("2025-01-15", response.data[2].day)
        assertEquals(300, response.data[2].visits)
        assertEquals(150, response.data[2].sessions)
    }

    /**
     * Tests the [AnalyticsService.getTraffic] method when the repository returns no result.
     */
    @Test
    fun `should return empty traffic data when no records returned`() {
        // Arrange
        val limit = 5
        val pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date"))

        val emptyPage: Page<TrafficDailyEntity> = PageImpl(emptyList())
        Mockito.`when`(trafficRepo.findAll(pageRequest)).thenReturn(emptyPage)

        // Act
        val response = analyticsService.getTraffic(limit)

        // Assert
        assertEquals(0, response.data.size)
    }

    /**
     * Tests the [AnalyticsService.getTraffic] method with a limit of 1.
     */
    @Test
    fun `should return single traffic data point when limit is one`() {
        // Arrange
        val limit = 1
        val pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date"))

        val trafficEntity = TrafficDailyEntity().apply {
            date = "2025-01-15"
            visits = 400
            sessions = 200
        }
        val page: Page<TrafficDailyEntity> = PageImpl(listOf(trafficEntity))

        Mockito.`when`(trafficRepo.findAll(pageRequest)).thenReturn(page)

        // Act
        val response = analyticsService.getTraffic(limit)

        // Assert
        assertEquals(1, response.data.size)
        assertEquals("2025-01-15", response.data[0].day)
        assertEquals(400, response.data[0].visits)
        assertEquals(200, response.data[0].sessions)
    }
}
