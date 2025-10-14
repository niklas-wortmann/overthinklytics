package com.overthinklytics.analytics.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

data class KpiSnapshot(
    val capturedAt: LocalDateTime,
    val totalUsers: Int,
    val sessions: Int,
    val conversionPct: Double,
    val revenueCents: Int
)

@Repository
class KpiRepository(private val jdbcTemplate: JdbcTemplate) {

    fun findLatest(): KpiSnapshot? {
        val sql = """
            SELECT capturedAt, totalUsers, sessions, conversionPct, revenueCents
            FROM KpiSnapshot
            ORDER BY capturedAt DESC
            LIMIT 1
        """.trimIndent()
        val rows = jdbcTemplate.query(sql) { rs, _ ->
            KpiSnapshot(
                capturedAt = rs.getTimestamp("capturedAt").toLocalDateTime(),
                totalUsers = rs.getInt("totalUsers"),
                sessions = rs.getInt("sessions"),
                conversionPct = rs.getDouble("conversionPct"),
                revenueCents = rs.getInt("revenueCents"),
            )
        }
        return rows.firstOrNull()
    }
}
