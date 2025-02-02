package com.hunhui.ticketworld.domain.reservation

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import java.util.UUID

class Reservations(
    val tryReserveUserId: UUID,
    val reservations: List<Reservation>,
) {
    init {
        if (reservations.isEmpty()) throw BusinessException(ReservationErrorCode.INVALID_RESERVATIONS)
        if (roundIdNotUnified) throw BusinessException(ReservationErrorCode.INVALID_RESERVATIONS)
    }

    val roundId: UUID
        get() = reservations.first().roundId

    val priceIdCountMap: Map<UUID, Int>
        get() = reservations.groupingBy { it.performancePriceId }.eachCount()

    fun tempReserve(): Reservations =
        Reservations(
            tryReserveUserId = tryReserveUserId,
            reservations = reservations.map { it.tempReserve(tryReserveUserId) },
        )

    fun confirmReserve(paymentId: UUID): Reservations =
        Reservations(
            tryReserveUserId = tryReserveUserId,
            reservations = reservations.map { it.confirmReserve(tryReserveUserId, paymentId) },
        )

    fun getPriceById(priceId: UUID): Money = reservations.first { it.performancePriceId == priceId }.price

    /** 선택된 예매들의 roundId가 통일되지 않았다면 True */
    private val roundIdNotUnified: Boolean
        get() = reservations.map { it.roundId }.distinct().size != 1
}
