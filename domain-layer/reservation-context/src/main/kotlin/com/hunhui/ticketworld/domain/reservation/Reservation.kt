package com.hunhui.ticketworld.domain.reservation

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import java.util.UUID

class Reservation(
    val tickets: List<Ticket>,
) {
    init {
        if (tickets.isEmpty()) throw BusinessException(ReservationErrorCode.INVALID_RESERVATIONS)
        if (roundIdNotUnified) throw BusinessException(ReservationErrorCode.INVALID_RESERVATIONS)
    }

    val roundId: UUID
        get() = tickets.first().roundId

    val priceIdCountMap: Map<UUID, Int>
        get() = tickets.groupingBy { it.performancePriceId }.eachCount()

    fun tempReserve(tryReserveUserId: UUID) =
        Reservation(
            tickets = tickets.map { it.tempReserve(tryReserveUserId) },
        )

    fun confirmReserve(
        paymentId: UUID,
        tryReserveUserId: UUID,
    ) = Reservation(
        tickets = tickets.map { it.confirmReserve(tryReserveUserId, paymentId) },
    )

    fun getPriceById(priceId: UUID): Money = tickets.first { it.performancePriceId == priceId }.price

    /** 선택된 예매들의 roundId가 통일되지 않았다면 True */
    private val roundIdNotUnified: Boolean
        get() = tickets.map { it.roundId }.distinct().size != 1
}
