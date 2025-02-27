package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.infra.jpa.entity.PaymentEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

internal interface PaymentJpaRepository : JpaRepository<PaymentEntity, UUID> {
    @Query(
        """
        SELECT p
        FROM PaymentEntity p
        WHERE p.userId = :userId
        AND p.performanceRoundId = :performanceRoundId
        AND p.status = 'COMPLETED'
        """,
    )
    fun findAllByUserIdAndPerformanceRoundId(
        userId: UUID,
        performanceRoundId: UUID,
    ): List<PaymentEntity>

    @Query(
        """
        SELECT p
        FROM PaymentEntity p
        WHERE p.userId = :userId
        AND p.performanceRoundId = :performanceRoundId
        AND p.status = 'COMPLETED'
        """,
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findAllByUserIdAndPerformanceRoundIdWithPessimistic(
        userId: UUID,
        performanceRoundId: UUID,
    ): List<PaymentEntity>
}
