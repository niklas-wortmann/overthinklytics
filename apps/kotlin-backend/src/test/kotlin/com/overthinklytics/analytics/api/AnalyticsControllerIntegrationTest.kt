package com.overthinklytics.analytics.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setupDb() {
        // Create tables if not exist (SQLite)
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS KpiSnapshot (
                capturedAt TEXT PRIMARY KEY,
                totalUsers INTEGER NOT NULL,
                sessions INTEGER NOT NULL,
                conversionPct REAL NOT NULL,
                revenueCents INTEGER NOT NULL
            )
            """.trimIndent()
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS TrafficDaily (
                date TEXT PRIMARY KEY,
                visits INTEGER NOT NULL,
                sessions INTEGER NOT NULL
            )
            """.trimIndent()
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS RevenueDaily (
                date TEXT PRIMARY KEY,
                valueCents INTEGER NOT NULL
            )
            """.trimIndent()
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS SignupByChannel (
                year INTEGER NOT NULL,
                month INTEGER NOT NULL,
                channel TEXT NOT NULL,
                signups INTEGER NOT NULL,
                PRIMARY KEY (year, month, channel)
            )
            """.trimIndent()
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS DeviceShare (
                snapshotDate TEXT NOT NULL,
                device TEXT NOT NULL,
                sharePct REAL NOT NULL,
                PRIMARY KEY (snapshotDate, device)
            )
            """.trimIndent()
        )

        // Cleanup
        jdbcTemplate.execute("DELETE FROM KpiSnapshot")
        jdbcTemplate.execute("DELETE FROM TrafficDaily")
        jdbcTemplate.execute("DELETE FROM RevenueDaily")
        jdbcTemplate.execute("DELETE FROM SignupByChannel")
        jdbcTemplate.execute("DELETE FROM DeviceShare")
    }

    @Test
    fun `kpis returns empty when no data`() {
        mockMvc.perform(get("/api/analytics/kpis").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.kpis").isArray)
            .andExpect(jsonPath("$.kpis.length()").value(0))
    }

    @Test
    fun `kpis returns formatted values`() {
        jdbcTemplate.update(
            "INSERT INTO KpiSnapshot (capturedAt, totalUsers, sessions, conversionPct, revenueCents) VALUES (?, ?, ?, ?, ?)",
            "2025-10-15T10:00:00", 12000, 3456, 7.8, 1_234_500
        )

        mockMvc.perform(get("/api/analytics/kpis").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.kpis[0].label").value("Total Users"))
            .andExpect(jsonPath("$.kpis[0].value").value("12,000"))
            .andExpect(jsonPath("$.kpis[1].label").value("Sessions"))
            .andExpect(jsonPath("$.kpis[1].value").value("3,456"))
            .andExpect(jsonPath("$.kpis[2].label").value("Conversion"))
            .andExpect(jsonPath("$.kpis[2].value").value("7.8%"))
            .andExpect(jsonPath("$.kpis[3].label").value("Revenue"))
            .andExpect(jsonPath("$.kpis[3].value").value("$1.2k"))
    }

    @Test
    fun `traffic returns ascending data and validates limit`() {
        // Insert 3 days
        jdbcTemplate.update("INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)", "2025-01-13", 10, 5)
        jdbcTemplate.update("INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)", "2025-01-14", 20, 10)
        jdbcTemplate.update("INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)", "2025-01-15", 30, 15)

        mockMvc.perform(get("/api/analytics/traffic").param("limit", "2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].day").value("2025-01-14"))
            .andExpect(jsonPath("$.data[1].day").value("2025-01-15"))

        // Validation: limit < 1
        mockMvc.perform(get("/api/analytics/traffic").param("limit", "0"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())

        // Validation: limit > 60
        mockMvc.perform(get("/api/analytics/traffic").param("limit", "61"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `signups returns latest month channel data`() {
        // Older month
        jdbcTemplate.update("INSERT INTO SignupByChannel (year, month, channel, signups) VALUES (?, ?, ?, ?)", 2025, 8, "Email", 10)
        // Latest month rows
        jdbcTemplate.update("INSERT INTO SignupByChannel (year, month, channel, signups) VALUES (?, ?, ?, ?)", 2025, 9, "Ads", 25)
        jdbcTemplate.update("INSERT INTO SignupByChannel (year, month, channel, signups) VALUES (?, ?, ?, ?)", 2025, 9, "Referral", 40)
        jdbcTemplate.update("INSERT INTO SignupByChannel (year, month, channel, signups) VALUES (?, ?, ?, ?)", 2025, 9, "Email", 30)

        mockMvc.perform(get("/api/analytics/signups"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].channel").value("Ads"))
            .andExpect(jsonPath("$.data[1].channel").value("Email"))
            .andExpect(jsonPath("$.data[2].channel").value("Referral"))
    }

    @Test
    fun `revenue returns ascending data and validates limit`() {
        jdbcTemplate.update("INSERT INTO RevenueDaily (date, valueCents) VALUES (?, ?)", "2025-01-13", 100)
        jdbcTemplate.update("INSERT INTO RevenueDaily (date, valueCents) VALUES (?, ?)", "2025-01-14", 200)
        jdbcTemplate.update("INSERT INTO RevenueDaily (date, valueCents) VALUES (?, ?)", "2025-01-15", 300)

        mockMvc.perform(get("/api/analytics/revenue").param("limit", "2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].day").value("2025-01-14"))
            .andExpect(jsonPath("$.data[1].day").value("2025-01-15"))
            .andExpect(jsonPath("$.data[1].value").value(3.0))

        mockMvc.perform(get("/api/analytics/revenue").param("limit", "0"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `device share returns latest snapshot`() {
        // Older snapshot
        jdbcTemplate.update("INSERT INTO DeviceShare (snapshotDate, device, sharePct) VALUES (?, ?, ?)", "2025-10-14T12:00:00Z", "Desktop", 55.0)
        // Latest snapshot
        jdbcTemplate.update("INSERT INTO DeviceShare (snapshotDate, device, sharePct) VALUES (?, ?, ?)", "2025-10-15T12:00:00Z", "Mobile", 60.0)
        jdbcTemplate.update("INSERT INTO DeviceShare (snapshotDate, device, sharePct) VALUES (?, ?, ?)", "2025-10-15T12:00:00Z", "Desktop", 40.0)

        mockMvc.perform(get("/api/analytics/device-share"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].name").value("Desktop"))
            .andExpect(jsonPath("$.data[1].name").value("Mobile"))
    }
}
