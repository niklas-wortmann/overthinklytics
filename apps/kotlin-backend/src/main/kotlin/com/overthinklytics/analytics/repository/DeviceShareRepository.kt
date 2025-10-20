package com.overthinklytics.analytics.repository

import com.overthinklytics.analytics.entity.DeviceShareEntity
import com.overthinklytics.analytics.entity.DeviceShareId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceShareRepository : JpaRepository<DeviceShareEntity, DeviceShareId> {
    fun findTopByOrderById_SnapshotDateDesc(): DeviceShareEntity?
    fun findById_SnapshotDateOrderById_DeviceAsc(snapshotDate: String): List<DeviceShareEntity>
}
