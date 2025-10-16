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
@Import(SignupRepository::class)
class SignupRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var signupRepository: SignupRepository

    @BeforeEach
    fun setUp() {
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
        jdbcTemplate.execute("DELETE FROM SignupByChannel")
    }

    @Test
    fun `findLatestMonth returns empty when table empty`() {
        val result = signupRepository.findLatestMonth()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findLatestMonth returns rows for most recent year-month ordered by channel`() {
        // Older month
        jdbcTemplate.update(
            "INSERT INTO SignupByChannel (year, month, channel, signups) VALUES (?, ?, ?, ?)",
            2025, 8, "Email", 10
        )
        // Latest month rows
        jdbcTemplate.update(
            "INSERT INTO SignupByChannel (year, month, channel, signups) VALUES (?, ?, ?, ?)",
            2025, 9, "Ads", 25
        )
        jdbcTemplate.update(
            "INSERT INTO SignupByChannel (year, month, channel, signups) VALUES (?, ?, ?, ?)",
            2025, 9, "Referral", 40
        )
        jdbcTemplate.update(
            "INSERT INTO SignupByChannel (year, month, channel, signups) VALUES (?, ?, ?, ?)",
            2025, 9, "Email", 30
        )

        val result = signupRepository.findLatestMonth()
        assertEquals(3, result.size)
        // Ordered by channel ASC: Ads, Email, Referral
        assertEquals("Ads", result[0].channel)
        assertEquals(25, result[0].signups)
        assertEquals("Email", result[1].channel)
        assertEquals(30, result[1].signups)
        assertEquals("Referral", result[2].channel)
        assertEquals(40, result[2].signups)
    }
}
