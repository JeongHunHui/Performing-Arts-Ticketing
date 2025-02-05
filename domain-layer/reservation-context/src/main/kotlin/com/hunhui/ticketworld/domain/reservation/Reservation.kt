package com.hunhui.ticketworld.domain.reservation

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.ROUND_ID_NOT_UNIFIED
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.TICKET_IS_EMPTY
import java.time.LocalDateTime
import java.util.UUID

class Reservation(
    val id: UUID,
    val performanceId: UUID,
    val userId: UUID,
    val paymentId: UUID?,
    val date: LocalDateTime?,
    val tickets: List<Ticket>,
) {
    init {
        if (tickets.isEmpty()) throw BusinessException(TICKET_IS_EMPTY)
        if (roundIdNotUnified) throw BusinessException(ROUND_ID_NOT_UNIFIED)
    }

    companion object {
        fun create(
            tickets: List<Ticket>,
            userId: UUID,
            performanceId: UUID,
        ): Reservation {
            val id: UUID = UUID.randomUUID()
            return Reservation(
                id = id,
                performanceId = performanceId,
                userId = userId,
                paymentId = null,
                tickets = tickets.map { it.tempReserve(id) },
                date = null,
            )
        }
    }

    val roundId: UUID
        get() = tickets.first().performanceRoundId

    val seatGradeIdCountMap: Map<UUID, Int>
        get() = tickets.groupingBy { it.seatGradeId }.eachCount()

    val isExpired: Boolean
        get() = tickets.any { it.isExpired }

    fun confirm(
        userId: UUID,
        paymentId: UUID,
    ): Reservation =
        Reservation(
            id = id,
            performanceId = performanceId,
            tickets = tickets.map { it.confirmReserve() },
            userId = userId,
            paymentId = paymentId,
            date = LocalDateTime.now(),
        )

    /** 선택된 예매들의 roundId가 통일되지 않았다면 True */
    private val roundIdNotUnified: Boolean
        get() = tickets.map { it.performanceRoundId }.distinct().size != 1
}
