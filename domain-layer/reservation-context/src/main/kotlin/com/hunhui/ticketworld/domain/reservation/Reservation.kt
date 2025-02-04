package com.hunhui.ticketworld.domain.reservation

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import java.time.LocalDateTime
import java.util.UUID

class Reservation(
    val id: UUID,
    val performanceId: UUID,
    val userId: UUID,
    val paymentId: UUID?,
    val tickets: List<Ticket>,
    val reservedAt: LocalDateTime?,
) {
    init {
        if (tickets.isEmpty()) throw BusinessException(ReservationErrorCode.INVALID_RESERVATION)
        if (roundIdNotUnified) throw BusinessException(ReservationErrorCode.INVALID_RESERVATION)
    }

    companion object {
        fun createTempReservation(
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
                reservedAt = null,
            )
        }
    }

    val roundId: UUID
        get() = tickets.first().roundId

    val priceIdCountMap: Map<UUID, Int>
        get() = tickets.groupingBy { it.performancePriceId }.eachCount()

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
            reservedAt = LocalDateTime.now(),
        )

    fun getPriceById(priceId: UUID): Money = tickets.first { it.performancePriceId == priceId }.price

    /** 선택된 예매들의 roundId가 통일되지 않았다면 True */
    private val roundIdNotUnified: Boolean
        get() = tickets.map { it.roundId }.distinct().size != 1
}
