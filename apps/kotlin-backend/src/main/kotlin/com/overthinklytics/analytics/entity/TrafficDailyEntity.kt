package com.overthinklytics.analytics.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * JPA entity for table `TrafficDaily`.
 */
@Entity
@Table(name = "TrafficDaily")
open class TrafficDailyEntity() {
    @Id
    @Column(name = "date", nullable = false, length = 255)
    open lateinit var date: String

    @Column(name = "visits", nullable = false)
    open var visits: Int = 0

    @Column(name = "sessions", nullable = false)
    open var sessions: Int = 0
}
