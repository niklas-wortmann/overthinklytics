package com.overthinklytics.analytics.repository

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@JdbcTest
@Import(DeviceShareRepository::class)
class DeviceShareRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var deviceShareRepository: DeviceShareRepository

    @BeforeEach
    fun setUp() {
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
        jdbcTemplate.execute("DELETE FROM DeviceShare")
    }

    @Test
    fun `findLatestSnapshot returns empty when no snapshots`() {
        val result = deviceShareRepository.findLatestSnapshot()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findLatestSnapshot returns rows for latest snapshot ordered by device`() {
        // Older snapshot
        jdbcTemplate.update(
            "INSERT INTO DeviceShare (snapshotDate, device, sharePct) VALUES (?, ?, ?)",
            "2025-10-14T12:00:00Z", "Desktop", 55.0
        )
        // Latest snapshot rows
        jdbcTemplate.update(
            "INSERT INTO DeviceShare (snapshotDate, device, sharePct) VALUES (?, ?, ?)",
            "2025-10-15T12:00:00Z", "Mobile", 60.0
        )
        jdbcTemplate.update(
            "INSERT INTO DeviceShare (snapshotDate, device, sharePct) VALUES (?, ?, ?)",
            "2025-10-15T12:00:00Z", "Desktop", 40.0
        )
        jdbcTemplate.update(
            "INSERT INTO DeviceShare (snapshotDate, device, sharePct) VALUES (?, ?, ?)",
            "2025-10-15T12:00:00Z", "Tablet", 0.0
        )

        val result = deviceShareRepository.findLatestSnapshot()
        assertEquals(3, result.size)
        // Ordered by device ASC: Desktop, Mobile, Tablet
        assertEquals("Desktop", result[0].device)
        assertEquals(40.0, result[0].sharePct)
        assertEquals("Mobile", result[1].device)
        assertEquals(60.0, result[1].sharePct)
        assertEquals("Tablet", result[2].device)
        assertEquals(0.0, result[2].sharePct)
    }
}
