package com.overthinklytics.analytics.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SignupRepository(private val jdbcTemplate: JdbcTemplate) {

    data class SignupRow(
        val channel: String,
        val signups: Int
    )

    fun findLatestMonth(): List<SignupRow> {
        // Determine latest (year, month) present, then fetch all channels for that month
        val ym = jdbcTemplate.query("""
            SELECT year, month
            FROM SignupByChannel
            ORDER BY year DESC, month DESC
            LIMIT 1
        """.trimIndent()) { rs, _ -> rs.getInt("year") to rs.getInt("month") }
            .firstOrNull() ?: return emptyList()

        val (year, month) = ym
        val sql = """
            SELECT channel, signups
            FROM SignupByChannel
            WHERE year = ? AND month = ?
            ORDER BY channel ASC
        """.trimIndent()
        return jdbcTemplate.query(sql, arrayOf(year, month)) { rs, _ ->
            SignupRow(
                channel = rs.getString("channel"),
                signups = rs.getInt("signups"),
            )
        }
    }
}
