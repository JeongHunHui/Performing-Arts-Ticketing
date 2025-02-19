package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.TicketEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

internal interface TicketJpaRepository : JpaRepository<TicketEntity, UUID> {
    fun findAllByPerformanceRoundIdAndSeatAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<TicketEntity>

    @Query(
        """
        SELECT t 
        FROM TicketEntity t 
        WHERE t.id IN :ids
        """,
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findTicketsByIdsWithPessimistic(ids: List<UUID>): List<TicketEntity>

    @Query(
        """
        SELECT t
        FROM ReservationEntity r
        JOIN r.tickets t
        WHERE t.performanceRoundId = :roundId
        AND r.userId = :userId
        AND t.isPaid = true
        """,
    )
    fun getPaidTicketsByRoundIdAndUserId(
        roundId: UUID,
        userId: UUID,
    ): List<TicketEntity>

    @Query(
        """
        SELECT t
        FROM ReservationEntity r
        JOIN r.tickets t
        WHERE t.performanceRoundId = :roundId
        AND r.userId = :userId
        AND t.isPaid = true
        """,
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun getPaidTicketsByRoundIdAndUserIdWithPessimistic(
        roundId: UUID,
        userId: UUID,
    ): List<TicketEntity>
}
