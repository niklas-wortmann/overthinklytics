package com.overthinklytics.analytics.repository

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@JdbcTest
@Import(TrafficRepository::class)
class TrafficRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var trafficRepository: TrafficRepository

    @BeforeEach
    fun setUp() {
        // Create the table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS TrafficDaily (
                date TEXT PRIMARY KEY,
                visits INTEGER NOT NULL,
                sessions INTEGER NOT NULL
            )
        """.trimIndent())

        // Clear any existing data
        jdbcTemplate.execute("DELETE FROM TrafficDaily")
    }

    @Test
    fun `findRecent returns empty list when no data exists`() {
        val result = trafficRepository.findRecent(10)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findRecent returns data in ascending chronological order`() {
        // Insert test data
        jdbcTemplate.update(
            "INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)",
            "2025-01-15", 100, 50
        )
        jdbcTemplate.update(
            "INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)",
            "2025-01-14", 80, 40
        )
        jdbcTemplate.update(
            "INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)",
            "2025-01-13", 60, 30
        )

        val result = trafficRepository.findRecent(10)

        assertEquals(3, result.size)
        // Should be in ascending order (oldest first)
        assertEquals(LocalDate.parse("2025-01-13"), result[0].date)
        assertEquals(60, result[0].visits)
        assertEquals(30, result[0].sessions)

        assertEquals(LocalDate.parse("2025-01-14"), result[1].date)
        assertEquals(80, result[1].visits)
        assertEquals(40, result[1].sessions)

        assertEquals(LocalDate.parse("2025-01-15"), result[2].date)
        assertEquals(100, result[2].visits)
        assertEquals(50, result[2].sessions)
    }

    @Test
    fun `findRecent respects limit parameter`() {
        // Insert 5 rows
        for (i in 1..5) {
            jdbcTemplate.update(
                "INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)",
                "2025-01-${10 + i}", i * 10, i * 5
            )
        }

        val result = trafficRepository.findRecent(3)

        assertEquals(3, result.size)
        // Should return the 3 most recent dates in ascending order
        assertEquals(LocalDate.parse("2025-01-13"), result[0].date)
        assertEquals(LocalDate.parse("2025-01-14"), result[1].date)
        assertEquals(LocalDate.parse("2025-01-15"), result[2].date)
    }

    @Test
    fun `findRecent handles limit greater than available rows`() {
        jdbcTemplate.update(
            "INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)",
            "2025-01-15", 100, 50
        )
        jdbcTemplate.update(
            "INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)",
            "2025-01-14", 80, 40
        )

        val result = trafficRepository.findRecent(10)

        assertEquals(2, result.size)
    }

    @Test
    fun `findRecent correctly parses date, visits, and sessions`() {
        jdbcTemplate.update(
            "INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)",
            "2025-10-15", 1234, 567
        )

        val result = trafficRepository.findRecent(1)

        assertEquals(1, result.size)
        assertEquals(LocalDate.parse("2025-10-15"), result[0].date)
        assertEquals(1234, result[0].visits)
        assertEquals(567, result[0].sessions)
    }

    @Test
    fun `findRecent returns single row correctly`() {
        jdbcTemplate.update(
            "INSERT INTO TrafficDaily (date, visits, sessions) VALUES (?, ?, ?)",
            "2025-01-15", 100, 50
        )

        val result = trafficRepository.findRecent(1)

        assertEquals(1, result.size)
        assertEquals(LocalDate.parse("2025-01-15"), result[0].date)
        assertEquals(100, result[0].visits)
        assertEquals(50, result[0].sessions)
    }
}
