package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.SeatAreaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface SeatAreaJpaRepository : JpaRepository<SeatAreaEntity, UUID> {
    fun findByPerformanceId(performanceId: UUID): List<SeatAreaEntity>

    fun findAllByPerformanceIdIn(performanceIds: List<UUID>): List<SeatAreaEntity>
}
