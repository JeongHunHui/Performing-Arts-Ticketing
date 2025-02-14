package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

internal interface ReservationJpaRepository : JpaRepository<ReservationEntity, UUID> {
    @Query(
        """
        SELECT COUNT(t)
        FROM ReservationEntity r
        JOIN r.tickets t
        WHERE t.performanceRoundId = :roundId
        AND r.userId = :userId
        AND t.isPaid = true
        """,
    )
    fun getPaidTicketCountByRoundIdAndUserId(
        roundId: UUID,
        userId: UUID,
    ): Int
}
