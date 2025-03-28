package com.hunhui.ticketworld.domain.payment

import java.time.LocalDateTime
import java.util.UUID

interface PaymentRepository {
    fun getById(id: UUID): Payment

    fun save(payment: Payment)

    fun saveAll(payments: List<Payment>)

    fun findAllPaymentByUserIdAndRoundId(
        userId: UUID,
        roundId: UUID,
    ): List<Payment>

    fun findAllPaymentByUserIdAndRoundIdWithPessimistic(
        userId: UUID,
        roundId: UUID,
    ): List<Payment>

    fun getPaymentCountsByTimeRange(
        previousStandardTime: LocalDateTime,
        standardTime: LocalDateTime,
    ): List<PaymentCount>
}
