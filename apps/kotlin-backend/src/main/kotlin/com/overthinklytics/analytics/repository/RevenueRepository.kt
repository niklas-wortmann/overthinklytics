package com.overthinklytics.analytics.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class RevenueRepository(private val jdbcTemplate: JdbcTemplate) {

    data class RevenueRow(
        val date: LocalDate,
        val valueCents: Int
    )

    fun findRecent(limit: Int): List<RevenueRow> {
        val sql = """
            SELECT date, valueCents
            FROM RevenueDaily
            ORDER BY date DESC
            LIMIT ?
        """.trimIndent()
        val rows = jdbcTemplate.query(sql, arrayOf(limit)) { rs, _ ->
            RevenueRow(
                date = java.time.LocalDate.parse(rs.getString("date")),
                valueCents = rs.getInt("valueCents"),
            )
        }
        return rows.asReversed()
    }
}
