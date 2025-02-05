package com.hunhui.ticketworld.application.dto.response

import java.util.UUID

data class PaymentStartResponse(
    val paymentId: UUID,
    val totalAmount: Long,
)
