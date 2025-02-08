package com.hunhui.ticketworld.domain.performance

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.INVALID_RESERVATION_FINISH_DATE
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.INVALID_RESERVATION_START_DATE
import java.time.LocalDateTime
import java.util.UUID

data class PerformanceRound(
    val id: UUID,
    val roundStartTime: LocalDateTime,
    val reservationStartTime: LocalDateTime,
    val reservationEndTime: LocalDateTime,
) {
    init {
        if (reservationStartTime.isAfter(reservationEndTime)) throw BusinessException(INVALID_RESERVATION_START_DATE)
        if (reservationEndTime.isAfter(roundStartTime)) throw BusinessException(INVALID_RESERVATION_FINISH_DATE)
    }

    companion object {
        fun create(
            roundStartTime: LocalDateTime,
            reservationStartTime: LocalDateTime,
            reservationEndTime: LocalDateTime,
        ) = PerformanceRound(
            id = UUID.randomUUID(),
            roundStartTime = roundStartTime,
            reservationStartTime = reservationStartTime,
            reservationEndTime = reservationEndTime,
        )
    }

    internal val isReservationAvailable: Boolean
        get() = LocalDateTime.now() in reservationStartTime..reservationEndTime
}
