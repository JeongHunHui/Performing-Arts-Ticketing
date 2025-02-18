package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.SeatGradeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

internal interface SeatGradeJpaRepository : JpaRepository<SeatGradeEntity, UUID> {
    @Query(
        """
        SELECT sg 
        FROM SeatGradeEntity sg 
        LEFT JOIN FETCH sg.discounts
        WHERE sg.id IN :ids
        """,
    )
    fun findAllById(ids: List<UUID>): List<SeatGradeEntity>

    @Query(
        """
        SELECT sg 
        FROM SeatGradeEntity sg 
        LEFT JOIN FETCH sg.discounts
        WHERE sg.performanceId = :performanceId
        """,
    )
    fun findAllByPerformanceId(performanceId: UUID): List<SeatGradeEntity>

    fun findAllByPerformanceIdIn(performanceIds: List<UUID>): List<SeatGradeEntity>
}
