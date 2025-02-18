package com.hunhui.ticketworld.domain.reservation

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.CANNOT_CONFIRM_RESERVE
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.CANNOT_TEMP_RESERVE
import java.time.LocalDateTime
import java.util.UUID

class Ticket(
    val id: UUID,
    val performanceRoundId: UUID,
    val seatAreaId: UUID,
    val seatPositionId: UUID,
    val seatGradeId: UUID,
    val reservationId: UUID?,
    val isPaid: Boolean,
    val expireTime: LocalDateTime,
    val version: Long,
) {
    companion object {
        fun create(
            performanceRoundId: UUID,
            seatAreaId: UUID,
            seatPositionId: UUID,
            seatGradeId: UUID,
        ): Ticket =
            Ticket(
                id = UUID.randomUUID(),
                performanceRoundId = performanceRoundId,
                seatAreaId = seatAreaId,
                seatPositionId = seatPositionId,
                seatGradeId = seatGradeId,
                reservationId = null,
                isPaid = false,
                expireTime = LocalDateTime.now(),
                version = 0L,
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
            performanceRoundId = performanceRoundId,
            seatAreaId = seatAreaId,
            seatPositionId = seatPositionId,
            seatGradeId = seatGradeId,
            reservationId = reservationId,
            isPaid = false,
            expireTime = getExpireTime(),
            version = version,
        )
    }

    internal fun confirmReserve(): Ticket {
        if (!canConfirmReserve) throw BusinessException(CANNOT_CONFIRM_RESERVE)
        return Ticket(
            id = id,
            performanceRoundId = performanceRoundId,
            seatAreaId = seatAreaId,
            seatPositionId = seatPositionId,
            seatGradeId = seatGradeId,
            reservationId = reservationId,
            isPaid = true,
            expireTime = expireTime,
            version = version,
        )
    }

    private val canConfirmReserve: Boolean
        get() = !isPaid && !isExpired
}
