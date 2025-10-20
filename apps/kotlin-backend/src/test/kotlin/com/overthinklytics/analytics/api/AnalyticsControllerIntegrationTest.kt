package com.overthinklytics.analytics.api

import com.overthinklytics.demo.DemoApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest(classes = [DemoApplication::class])
@AutoConfigureMockMvc
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
])
class AnalyticsControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setupDb() {
        // Drop tables if they exist (for clean state between tests)
        listOf("kpi_snapshot", "traffic_daily", "revenue_daily", "signup_by_channel", "device_share").forEach { table ->
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS \"$table\"")
            } catch (e: Exception) {
                // Ignore if table doesn't exist
            }
        }

        // Create tables if not exist (H2 compatible)
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS "kpi_snapshot" (
                "captured_at" VARCHAR(255) PRIMARY KEY,
                "total_users" INTEGER NOT NULL,
                "sessions" INTEGER NOT NULL,
                "conversion_pct" DOUBLE NOT NULL,
                "revenue_cents" INTEGER NOT NULL
            )
            """.trimIndent()
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS "traffic_daily" (
                "date" VARCHAR(255) PRIMARY KEY,
                "visits" INTEGER NOT NULL,
                "sessions" INTEGER NOT NULL
            )
            """.trimIndent()
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS "revenue_daily" (
                "date" VARCHAR(255) PRIMARY KEY,
                "value_cents" INTEGER NOT NULL
            )
            """.trimIndent()
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS "signup_by_channel" (
                "id" INTEGER AUTO_INCREMENT PRIMARY KEY,
                "year" INTEGER NOT NULL,
                "month" INTEGER NOT NULL,
                "channel" VARCHAR(255) NOT NULL,
                "signups" INTEGER NOT NULL,
                UNIQUE ("year", "month", "channel")
            )
            """.trimIndent()
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS "device_share" (
                "snapshot_date" VARCHAR(255) NOT NULL,
                "device" VARCHAR(255) NOT NULL,
                "share_pct" DOUBLE NOT NULL,
                PRIMARY KEY ("snapshot_date", "device")
            )
            """.trimIndent()
        )
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
            "INSERT INTO \"kpi_snapshot\" (\"captured_at\", \"total_users\", \"sessions\", \"conversion_pct\", \"revenue_cents\") VALUES (?, ?, ?, ?, ?)",
            "2025-10-15T10:00:00", 12000, 3456, 7.8, 123_450
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
        jdbcTemplate.update("INSERT INTO \"traffic_daily\" (\"date\", \"visits\", \"sessions\") VALUES (?, ?, ?)", "2025-01-13", 10, 5)
        jdbcTemplate.update("INSERT INTO \"traffic_daily\" (\"date\", \"visits\", \"sessions\") VALUES (?, ?, ?)", "2025-01-14", 20, 10)
        jdbcTemplate.update("INSERT INTO \"traffic_daily\" (\"date\", \"visits\", \"sessions\") VALUES (?, ?, ?)", "2025-01-15", 30, 15)

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
        jdbcTemplate.update("INSERT INTO \"signup_by_channel\" (\"year\", \"month\", \"channel\", \"signups\") VALUES (?, ?, ?, ?)", 2025, 8, "Email", 10)
        // Latest month rows
        jdbcTemplate.update("INSERT INTO \"signup_by_channel\" (\"year\", \"month\", \"channel\", \"signups\") VALUES (?, ?, ?, ?)", 2025, 9, "Ads", 25)
        jdbcTemplate.update("INSERT INTO \"signup_by_channel\" (\"year\", \"month\", \"channel\", \"signups\") VALUES (?, ?, ?, ?)", 2025, 9, "Referral", 40)
        jdbcTemplate.update("INSERT INTO \"signup_by_channel\" (\"year\", \"month\", \"channel\", \"signups\") VALUES (?, ?, ?, ?)", 2025, 9, "Email", 30)

        mockMvc.perform(get("/api/analytics/signups"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].channel").value("Ads"))
            .andExpect(jsonPath("$.data[1].channel").value("Email"))
            .andExpect(jsonPath("$.data[2].channel").value("Referral"))
    }

    @Test
    fun `revenue returns ascending data and validates limit`() {
        jdbcTemplate.update("INSERT INTO \"revenue_daily\" (\"date\", \"value_cents\") VALUES (?, ?)", "2025-01-13", 100)
        jdbcTemplate.update("INSERT INTO \"revenue_daily\" (\"date\", \"value_cents\") VALUES (?, ?)", "2025-01-14", 200)
        jdbcTemplate.update("INSERT INTO \"revenue_daily\" (\"date\", \"value_cents\") VALUES (?, ?)", "2025-01-15", 300)

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
        jdbcTemplate.update("INSERT INTO \"device_share\" (\"snapshot_date\", \"device\", \"share_pct\") VALUES (?, ?, ?)", "2025-10-14T12:00:00Z", "Desktop", 55.0)
        // Latest snapshot
        jdbcTemplate.update("INSERT INTO \"device_share\" (\"snapshot_date\", \"device\", \"share_pct\") VALUES (?, ?, ?)", "2025-10-15T12:00:00Z", "Mobile", 60.0)
        jdbcTemplate.update("INSERT INTO \"device_share\" (\"snapshot_date\", \"device\", \"share_pct\") VALUES (?, ?, ?)", "2025-10-15T12:00:00Z", "Desktop", 40.0)

        mockMvc.perform(get("/api/analytics/device-share"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].name").value("Desktop"))
            .andExpect(jsonPath("$.data[1].name").value("Mobile"))
    }
}
