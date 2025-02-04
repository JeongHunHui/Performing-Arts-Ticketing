package com.hunhui.ticketworld.application.dto.request

import com.hunhui.ticketworld.domain.payment.PaymentMethod
import java.util.UUID

data class StartPaymentRequest(
    val reservationId: UUID,
    val paymentRequestInfos: List<PaymentRequestInfo>,
    val paymentMethod: PaymentMethod,
    val userId: UUID,
) {
    fun getDiscountIds(): List<UUID> = paymentRequestInfos.mapNotNull { it.discountId }.distinct()
}

data class PaymentRequestInfo(
    val performancePriceId: UUID,
    val reservationCount: Int,
    val discountId: UUID?,
)
