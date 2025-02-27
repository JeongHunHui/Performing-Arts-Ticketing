package com.hunhui.ticketworld.domain.payment

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.payment.exception.PaymentErrorCode.CANNOT_COMPLETE
import java.util.UUID

class Payment(
    val id: UUID,
    val userId: UUID,
    val roundId: UUID,
    val status: PaymentStatus,
    val method: PaymentMethod,
    val items: MutableList<PaymentItem>,
) {
    companion object {
        fun create(
            userId: UUID,
            roundId: UUID,
            paymentMethod: PaymentMethod,
        ): Payment =
            Payment(
                id = UUID.randomUUID(),
                userId = userId,
                roundId = roundId,
                status = PaymentStatus.PENDING,
                method = paymentMethod,
                items = mutableListOf(),
            )
    }

    val totalAmount: Money
        get() = Money(items.sumOf { it.discountedPrice.amount })

    val ticketCount: Int
        get() = items.sumOf { it.reservationCount }

    fun addItem(
        seatGradeName: String,
        reservationCount: Int,
        discountName: String,
        originalPrice: Money,
        discountedPrice: Money,
    ) {
        items.add(
            PaymentItem(
                id = UUID.randomUUID(),
                seatGradeName = seatGradeName,
                reservationCount = reservationCount,
                discountName = discountName,
                originalPrice = originalPrice,
                discountedPrice = discountedPrice,
            ),
        )
    }

    fun complete(): Payment {
        if (status != PaymentStatus.PENDING) throw BusinessException(CANNOT_COMPLETE)
        return Payment(
            id = id,
            status = PaymentStatus.COMPLETED,
            userId = userId,
            roundId = roundId,
            method = method,
            items = items,
        )
    }

    fun cancel(): Payment =
        Payment(
            id = id,
            status = PaymentStatus.CANCELED,
            userId = userId,
            roundId = roundId,
            method = method,
            items = items,
        )
}
