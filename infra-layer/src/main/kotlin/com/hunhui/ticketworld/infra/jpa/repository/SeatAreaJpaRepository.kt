package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.SeatAreaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

internal interface SeatAreaJpaRepository : JpaRepository<SeatAreaEntity, UUID> {
    @Query(
        """
        SELECT sa
        FROM SeatAreaEntity sa
        JOIN FETCH sa.positions
        WHERE sa.performanceId = :performanceId
    """,
    )
    fun findByPerformanceId(performanceId: UUID): List<SeatAreaEntity>

    fun findAllByPerformanceIdIn(performanceIds: List<UUID>): List<SeatAreaEntity>
}
