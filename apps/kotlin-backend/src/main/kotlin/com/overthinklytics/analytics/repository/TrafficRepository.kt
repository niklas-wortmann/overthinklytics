package com.overthinklytics.analytics.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class TrafficRepository(private val jdbcTemplate: JdbcTemplate) {

    data class TrafficRow(
        val date: LocalDate,
        val visits: Int,
        val sessions: Int
    )

    fun findRecent(limit: Int): List<TrafficRow> {
        val sql = """
            SELECT date, visits, sessions
            FROM TrafficDaily
            ORDER BY date DESC
            LIMIT ?
        """.trimIndent()
        val rows = jdbcTemplate.query(sql, arrayOf(limit)) { rs, _ ->
            TrafficRow(
                date = java.time.LocalDate.parse(rs.getString("date")),
                visits = rs.getInt("visits"),
                sessions = rs.getInt("sessions"),
            )
        }
        // Return in ascending chronological order for nicer charts
        return rows.asReversed()
    }
}
