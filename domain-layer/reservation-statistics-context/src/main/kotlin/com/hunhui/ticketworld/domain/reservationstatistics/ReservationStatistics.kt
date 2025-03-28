package com.hunhui.ticketworld.domain.reservationstatistics

import java.time.LocalDateTime
import java.util.UUID

class ReservationStatistics(
    val performanceId: UUID,
    val standardTime: LocalDateTime,
    val count: Long,
)
