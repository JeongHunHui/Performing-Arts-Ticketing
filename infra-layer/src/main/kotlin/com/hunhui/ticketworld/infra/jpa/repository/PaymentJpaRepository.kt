package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.domain.payment.PaymentCount
import com.hunhui.ticketworld.infra.jpa.entity.PaymentEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
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

    @Query(
        """
        select new com.hunhui.ticketworld.domain.payment.PaymentCount(p.performanceId, :standardTime, sum(pi.reservationCount))
        from PaymentEntity p join p.items pi
        where p.status = 'COMPLETED' and p.paidAt >= :previousStandardTime and p.paidAt < :standardTime
        group by p.performanceId
        """,
    )
    fun getPaymentCountsByTimeRange(
        @Param("previousStandardTime") previousStandardTime: LocalDateTime,
        @Param("standardTime") standardTime: LocalDateTime,
    ): List<PaymentCount>
}
