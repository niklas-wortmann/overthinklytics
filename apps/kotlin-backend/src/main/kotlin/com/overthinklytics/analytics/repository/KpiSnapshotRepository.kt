package com.overthinklytics.analytics.repository

import com.overthinklytics.analytics.entity.KpiSnapshotEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface KpiSnapshotRepository : JpaRepository<KpiSnapshotEntity, String> {
    fun findTopByOrderByCapturedAtDesc(): KpiSnapshotEntity?
}
