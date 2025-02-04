package com.hunhui.ticketworld.application.dto.request

import java.util.UUID

data class CompletePaymentRequest(
    val paymentId: UUID,
    val userId: UUID,
    val reservationId: UUID,
)
