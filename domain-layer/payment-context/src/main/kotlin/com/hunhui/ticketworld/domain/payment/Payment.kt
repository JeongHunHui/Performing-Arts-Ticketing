package com.hunhui.ticketworld.domain.payment

import com.hunhui.ticketworld.common.vo.Money
import java.util.UUID

class Payment(
    val id: UUID,
    val userId: UUID,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val paymentInfos: List<PaymentInfo>,
) {
    companion object {
        fun create(
            userId: UUID,
            paymentMethod: PaymentMethod,
            paymentInfos: List<PaymentInfo>,
        ): Payment =
            Payment(
                id = UUID.randomUUID(),
                userId = userId,
                status = PaymentStatus.PENDING,
                paymentMethod = paymentMethod,
                paymentInfos = paymentInfos,
            )
    }

    val totalAmount: Money
        get() = Money(paymentInfos.sumOf { it.paymentAmount.amount })

    fun complete(): Payment =
        Payment(
            id = id,
            status = PaymentStatus.COMPLETED,
            userId = userId,
            paymentMethod = paymentMethod,
            paymentInfos = paymentInfos,
        )
}
