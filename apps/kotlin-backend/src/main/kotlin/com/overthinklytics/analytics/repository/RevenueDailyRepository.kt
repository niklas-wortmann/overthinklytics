package com.overthinklytics.analytics.repository

import com.overthinklytics.analytics.entity.RevenueDailyEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RevenueDailyRepository : JpaRepository<RevenueDailyEntity, String>
