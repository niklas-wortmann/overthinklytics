package com.overthinklytics.analytics.entity

import jakarta.persistence.*

/**
 * JPA entity for table `SignupByChannel` aligned with Prisma schema.
 * Columns: id (PK, autoincrement), year, month, channel, signups.
 */
@Entity
@Table(
    name = "SignupByChannel",
    uniqueConstraints = [
        UniqueConstraint(name = "uniq_month_channel", columnNames = ["year", "month", "channel"])
    ]
)
open class SignupByChannelEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    open var id: Int? = null

    @Column(name = "year", nullable = false)
    open var year: Int = 0

    @Column(name = "month", nullable = false)
    open var month: Int = 0

    @Column(name = "channel", nullable = false, length = 255)
    open lateinit var channel: String

    @Column(name = "signups", nullable = false)
    open var signups: Int = 0
}
