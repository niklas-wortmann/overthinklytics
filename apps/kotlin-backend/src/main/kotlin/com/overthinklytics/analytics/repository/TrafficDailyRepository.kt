package com.overthinklytics.analytics.repository

import com.overthinklytics.analytics.entity.TrafficDailyEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrafficDailyRepository : JpaRepository<TrafficDailyEntity, String>
