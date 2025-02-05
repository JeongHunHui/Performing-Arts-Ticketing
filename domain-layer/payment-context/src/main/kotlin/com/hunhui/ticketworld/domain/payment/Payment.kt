package com.hunhui.ticketworld.domain.payment

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.payment.exception.PaymentErrorCode.CANNOT_COMPLETE
import java.util.UUID

class Payment(
    val id: UUID,
    val userId: UUID,
    val status: PaymentStatus,
    val method: PaymentMethod,
    val items: List<PaymentItem>,
) {
    companion object {
        fun create(
            userId: UUID,
            paymentMethod: PaymentMethod,
            paymentItems: List<PaymentItem>,
        ): Payment =
            Payment(
                id = UUID.randomUUID(),
                userId = userId,
                status = PaymentStatus.PENDING,
                method = paymentMethod,
                items = paymentItems,
            )
    }

    val totalAmount: Money
        get() = Money(items.sumOf { it.paymentAmount.amount })

    fun complete(): Payment {
        if (status != PaymentStatus.PENDING) throw BusinessException(CANNOT_COMPLETE)
        return Payment(
            id = id,
            status = PaymentStatus.COMPLETED,
            userId = userId,
            method = method,
            items = items,
        )
    }

    fun cancel(): Payment =
        Payment(
            id = id,
            status = PaymentStatus.CANCELED,
            userId = userId,
            method = method,
            items = items,
        )
}
