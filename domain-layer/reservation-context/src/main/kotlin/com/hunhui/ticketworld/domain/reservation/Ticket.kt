package com.hunhui.ticketworld.domain.reservation

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.CANNOT_CONFIRM_RESERVE
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.CANNOT_TEMP_RESERVE
import java.time.LocalDateTime
import java.util.UUID

class Ticket(
    val id: UUID,
    val roundId: UUID,
    val areaId: UUID,
    val seatId: UUID,
    val priceId: UUID,
    val reservationId: UUID?,
    val price: Money,
    val isPaid: Boolean,
    val expireTime: LocalDateTime,
) {
    companion object {
        fun create(
            roundId: UUID,
            areaId: UUID,
            seatId: UUID,
            priceId: UUID,
            price: Money,
        ): Ticket =
            Ticket(
                id = UUID.randomUUID(),
                roundId = roundId,
                areaId = areaId,
                seatId = seatId,
                priceId = priceId,
                reservationId = null,
                price = price,
                isPaid = false,
                expireTime = LocalDateTime.now(),
            )

        private const val EXPIRE_MINUTES = 7L

        private fun getExpireTime(): LocalDateTime = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)
    }

    /** 만료시간이 없거나 이미 지났으면 true */
    internal val isExpired: Boolean
        get() = expireTime.isBefore(LocalDateTime.now())

    /** 결제되지 않은 티켓이고 만료되었으면 true */
    val canTempReserve: Boolean
        get() = !isPaid && isExpired

    internal fun tempReserve(reservationId: UUID): Ticket {
        if (!canTempReserve) throw BusinessException(CANNOT_TEMP_RESERVE)
        return Ticket(
            id = id,
            roundId = roundId,
            areaId = areaId,
            seatId = seatId,
            priceId = priceId,
            reservationId = reservationId,
            price = price,
            isPaid = false,
            expireTime = getExpireTime(),
        )
    }

    internal fun confirmReserve(): Ticket {
        if (!canConfirmReserve) throw BusinessException(CANNOT_CONFIRM_RESERVE)
        return Ticket(
            id = id,
            roundId = roundId,
            areaId = areaId,
            seatId = seatId,
            priceId = priceId,
            reservationId = reservationId,
            price = price,
            isPaid = true,
            expireTime = expireTime,
        )
    }

    private val canConfirmReserve: Boolean
        get() = !isPaid && !isExpired
}
