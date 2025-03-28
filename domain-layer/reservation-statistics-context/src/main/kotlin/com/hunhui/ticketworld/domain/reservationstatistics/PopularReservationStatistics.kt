package com.hunhui.ticketworld.domain.reservationstatistics

import java.math.BigDecimal
import java.util.UUID

class PopularReservationStatistics(
    performanceId: String,
    val reservationRate: BigDecimal,
) {
    val performanceId: UUID = UUID.fromString(performanceId)
}
