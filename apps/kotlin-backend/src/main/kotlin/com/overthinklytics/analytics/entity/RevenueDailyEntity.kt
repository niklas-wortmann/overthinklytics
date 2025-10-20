package com.overthinklytics.analytics.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * JPA entity for table `RevenueDaily`.
 */
@Entity
@Table(name = "RevenueDaily")
open class RevenueDailyEntity() {
    @Id
    @Column(name = "date", nullable = false, length = 255)
    open lateinit var date: String

    @Column(name = "valueCents", nullable = false)
    open var valueCents: Int = 0
}
