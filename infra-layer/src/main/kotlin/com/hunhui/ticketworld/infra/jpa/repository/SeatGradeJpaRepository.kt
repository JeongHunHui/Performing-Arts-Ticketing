package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.SeatGradeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface SeatGradeJpaRepository : JpaRepository<SeatGradeEntity, UUID> {
    fun findAllByPerformanceId(performanceId: UUID): List<SeatGradeEntity>

    fun findAllByPerformanceIdIn(performanceIds: List<UUID>): List<SeatGradeEntity>
}
