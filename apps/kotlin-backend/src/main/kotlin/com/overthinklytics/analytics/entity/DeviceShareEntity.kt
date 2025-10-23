package com.overthinklytics.analytics.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault

@Embeddable
open class DeviceShareId() {
    @Column(name = "snapshotDate", nullable = false, length = 255)
    open lateinit var snapshotDate: String

    @Column(name = "device", nullable = false, length = 255)
    open lateinit var device: String
}

/**
 * JPA entity for table `DeviceShare`.
 */
@Entity
@Table(name = "DeviceShare")
open class DeviceShareEntity() {
    @EmbeddedId
    open lateinit var id: DeviceShareId

    @Column(name = "sharePct", nullable = false)
    open var sharePct: Double = 0.0


    @Column(name = "os", length = 255)
    open var os: String = ""
}
