package com.overthinklytics.analytics.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * JPA entity for table `KpiSnapshot`.
 * All date/time persisted as String for compatibility with SQLite/H2 setup.
 */
@Entity
@Table(name = "KpiSnapshot")
open class KpiSnapshotEntity() {
    @Id
    @Column(name = "capturedAt", nullable = false, length = 255)
    open lateinit var capturedAt: String

    @Column(name = "totalUsers", nullable = false)
    open var totalUsers: Int = 0

    @Column(name = "sessions", nullable = false)
    open var sessions: Int = 0

    @Column(name = "conversionPct", nullable = false)
    open var conversionPct: Double = 0.0

    @Column(name = "revenueCents", nullable = false)
    open var revenueCents: Int = 0
}
