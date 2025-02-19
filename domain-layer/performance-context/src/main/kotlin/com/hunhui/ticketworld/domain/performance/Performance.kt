package com.hunhui.ticketworld.domain.performance

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.ROUND_IS_EMPTY
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Performance(
    val id: UUID,
    val info: PerformanceInfo,
    val maxReservationCount: Int,
    val startDate: LocalDate,
    val finishDate: LocalDate,
    val rounds: List<PerformanceRound>,
) {
    companion object {
        fun create(
            performanceInfo: PerformanceInfo,
            maxReservationCount: Int,
            rounds: List<PerformanceRound>,
        ): Performance =
            Performance(
                id = UUID.randomUUID(),
                info = performanceInfo,
                maxReservationCount = maxReservationCount,
                rounds = rounds,
                startDate = rounds.minOfOrNull { it.roundStartTime.toLocalDate() } ?: throw BusinessException(ROUND_IS_EMPTY),
                finishDate = rounds.maxOfOrNull { it.roundStartTime.toLocalDate() } ?: throw BusinessException(ROUND_IS_EMPTY),
            )
    }

    val availableRounds: List<PerformanceRound>
        get() = rounds.filter { it.isReservationAvailable }
    val minimumReservationStartTime: LocalDateTime
        get() = rounds.minOf { it.reservationStartTime }

    fun isAvailableRoundId(roundId: UUID): Boolean = availableRounds.any { it.id == roundId }
}
