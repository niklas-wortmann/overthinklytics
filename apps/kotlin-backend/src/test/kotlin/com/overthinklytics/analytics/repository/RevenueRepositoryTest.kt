package com.overthinklytics.analytics.repository

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@JdbcTest
@Import(RevenueRepository::class)
class RevenueRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var revenueRepository: RevenueRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS RevenueDaily (
                date TEXT PRIMARY KEY,
                valueCents INTEGER NOT NULL
            )
            """.trimIndent()
        )
        jdbcTemplate.execute("DELETE FROM RevenueDaily")
    }

    @Test
    fun `findRecent returns empty when no rows`() {
        val result = revenueRepository.findRecent(5)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findRecent returns limited most recent in ascending order`() {
        for (i in 1..5) {
            jdbcTemplate.update(
                "INSERT INTO RevenueDaily (date, valueCents) VALUES (?, ?)",
                "2025-01-${10 + i}", i * 100
            )
        }
        val result = revenueRepository.findRecent(3)
        assertEquals(3, result.size)
        assertEquals(LocalDate.parse("2025-01-13"), result[0].date)
        assertEquals(LocalDate.parse("2025-01-14"), result[1].date)
        assertEquals(LocalDate.parse("2025-01-15"), result[2].date)
        assertEquals(300, result[0].valueCents)
        assertEquals(400, result[1].valueCents)
        assertEquals(500, result[2].valueCents)
    }
}
