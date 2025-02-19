package com.hunhui.ticketworld.application.dto.request

import java.util.UUID

data class PaymentCompleteRequest(
    val paymentId: UUID,
    val userId: UUID,
    val reservationId: UUID,
    val selectReservationLockMode: LockMode = LockMode.OPTIMISTIC,
    val selectTicketsLockMode: LockMode = LockMode.OPTIMISTIC,
)
