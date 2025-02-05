package com.hunhui.ticketworld.application.dto.request

import java.util.UUID

data class PaymentCompleteRequest(
    val paymentId: UUID,
    val userId: UUID,
    val reservationId: UUID,
)
