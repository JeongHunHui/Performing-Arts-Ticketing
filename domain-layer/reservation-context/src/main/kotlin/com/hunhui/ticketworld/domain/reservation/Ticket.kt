package com.hunhui.ticketworld.domain.reservation

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import java.time.LocalDateTime
import java.util.UUID

class Ticket(
    val id: UUID,
    val roundId: UUID,
    val seatAreaId: UUID,
    val seatId: UUID,
    val performancePriceId: UUID,
    val price: Money,
    val tempUserId: UUID?,
    val paymentId: UUID?,
    val reservationExpireTime: LocalDateTime,
) {
    companion object {
        private const val EXPIRE_MINUTES = 7L

        fun getExpireTime(): LocalDateTime = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)
    }

    /** 예매 가능 여부, 임시 예매가 만료되고 아직 결제되지 않은 경우 True */
    val canTempReserve: Boolean
        get() = isTempReservationExpired && !isPaid

    private fun canReserve(userId: UUID): Boolean = tempUserId == userId && !isPaid

    fun tempReserve(userId: UUID): Ticket {
        if (!canTempReserve) throw BusinessException(ReservationErrorCode.CANNOT_TEMP_RESERVE)
        return Ticket(
            id = id,
            roundId = roundId,
            seatAreaId = seatAreaId,
            seatId = seatId,
            performancePriceId = performancePriceId,
            price = price,
            tempUserId = userId,
            paymentId = paymentId,
            reservationExpireTime = getExpireTime(),
        )
    }

    fun reserve(
        userId: UUID,
        paymentId: UUID,
    ): Ticket {
        if (!canReserve(userId)) throw BusinessException(ReservationErrorCode.CANNOT_RESERVE)
        return Ticket(
            id = id,
            roundId = roundId,
            seatAreaId = seatAreaId,
            seatId = seatId,
            performancePriceId = performancePriceId,
            price = price,
            tempUserId = tempUserId,
            paymentId = paymentId,
            reservationExpireTime = LocalDateTime.now(),
        )
    }

    /** 만료시간이 없거나 이미 지났으면 true */
    private val isTempReservationExpired: Boolean
        get() = reservationExpireTime.isBefore(LocalDateTime.now())

    private val isPaid: Boolean
        get() = paymentId != null
}
