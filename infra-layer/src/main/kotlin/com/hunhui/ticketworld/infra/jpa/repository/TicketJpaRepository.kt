package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.TicketEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface TicketJpaRepository : JpaRepository<TicketEntity, UUID> {
    fun findAllByPerformanceRoundIdAndSeatAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<TicketEntity>
}
