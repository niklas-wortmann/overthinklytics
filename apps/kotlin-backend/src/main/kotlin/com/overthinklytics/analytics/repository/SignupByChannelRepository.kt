package com.overthinklytics.analytics.repository

import com.overthinklytics.analytics.entity.SignupByChannelEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SignupByChannelRepository : JpaRepository<SignupByChannelEntity, Int> {
    fun findTopByOrderByYearDescMonthDesc(): SignupByChannelEntity?
    fun findByYearAndMonthOrderByChannelAsc(year: Int, month: Int): List<SignupByChannelEntity>
}
