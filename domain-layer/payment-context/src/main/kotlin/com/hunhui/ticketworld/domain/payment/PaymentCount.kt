package com.hunhui.ticketworld.domain.payment

import java.time.LocalDateTime
import java.util.UUID

class PaymentCount(
    val performanceId: UUID,
    val standardTime: LocalDateTime,
    val count: Long,
)
