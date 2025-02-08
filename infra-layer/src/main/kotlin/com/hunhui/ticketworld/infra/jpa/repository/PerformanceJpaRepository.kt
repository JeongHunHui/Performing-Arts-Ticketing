package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.PerformanceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface PerformanceJpaRepository : JpaRepository<PerformanceEntity, UUID> {
    fun findByKopisId(kopisId: String): PerformanceEntity?
}
