package com.overthinklytics.analytics.repository

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

@JdbcTest
@Import(KpiRepository::class)
class KpiRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var kpiRepository: KpiRepository

    @BeforeEach
    fun setUp() {
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
        jdbcTemplate.execute("DELETE FROM KpiSnapshot")
    }

    @Test
    fun `findLatest returns null when table empty`() {
        val result = kpiRepository.findLatest()
        assertNull(result)
    }

    @Test
    fun `findLatest returns most recent snapshot`() {
        jdbcTemplate.update(
            "INSERT INTO KpiSnapshot (capturedAt, totalUsers, sessions, conversionPct, revenueCents) VALUES (?, ?, ?, ?, ?)",
            "2025-10-14T10:00:00", 1000, 500, 12.5, 123_456
        )
        jdbcTemplate.update(
            "INSERT INTO KpiSnapshot (capturedAt, totalUsers, sessions, conversionPct, revenueCents) VALUES (?, ?, ?, ?, ?)",
            "2025-10-15T09:00:00", 2000, 900, 11.0, 456_789
        )

        val result = kpiRepository.findLatest()!!
        assertEquals(LocalDateTime.parse("2025-10-15T09:00:00"), result.capturedAt)
        assertEquals(2000, result.totalUsers)
        assertEquals(900, result.sessions)
        assertEquals(11.0, result.conversionPct)
        assertEquals(456_789, result.revenueCents)
    }
}
