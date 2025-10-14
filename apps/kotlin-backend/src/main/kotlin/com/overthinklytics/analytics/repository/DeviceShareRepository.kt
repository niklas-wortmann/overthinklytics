package com.overthinklytics.analytics.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
// Note: SQLite JDBC has limited support for Java 8 time types; avoid OffsetDateTime here

@Repository
class DeviceShareRepository(private val jdbcTemplate: JdbcTemplate) {

    data class DeviceShareRow(
        val device: String,
        val sharePct: Double
    )

    fun findLatestSnapshot(): List<DeviceShareRow> {
        // Read latest snapshot as ISO-8601 text. SQLite orders ISO timestamps lexicographically.
        val snapshot: String = jdbcTemplate.query(
            """
            SELECT snapshotDate
            FROM DeviceShare
            ORDER BY snapshotDate DESC
            LIMIT 1
            """.trimIndent()
        ) { rs, _ -> rs.getString("snapshotDate") }
            .firstOrNull() ?: return emptyList()

        val sql = """
            SELECT device, sharePct
            FROM DeviceShare
            WHERE snapshotDate = ?
            ORDER BY device ASC
        """.trimIndent()
        return jdbcTemplate.query(sql, arrayOf(snapshot)) { rs, _ ->
            DeviceShareRow(
                device = rs.getString("device"),
                sharePct = rs.getDouble("sharePct"),
            )
        }
    }
}
