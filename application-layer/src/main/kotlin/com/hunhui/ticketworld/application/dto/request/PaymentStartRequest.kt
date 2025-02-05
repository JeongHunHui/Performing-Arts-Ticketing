package com.hunhui.ticketworld.application.dto.request

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.payment.PaymentMethod
import com.hunhui.ticketworld.domain.payment.exception.PaymentErrorCode.INVALID_START_PAYMENT_REQUEST
import com.hunhui.ticketworld.domain.reservation.Reservation
import java.util.UUID

data class PaymentStartRequest(
    val reservationId: UUID,
    val paymentItems: List<PaymentItemRequest>,
    val paymentMethod: PaymentMethod,
    val userId: UUID,
) {
    data class PaymentItemRequest(
        val seatGradeId: UUID,
        val reservationCount: Int,
        val discountId: UUID?,
    )

    fun checkSeatGradeCountByReservation(reservation: Reservation) {
        if (reservation.seatGradeIdCountMap != seatGradeIdCountMap) throw BusinessException(INVALID_START_PAYMENT_REQUEST)
    }

    private val seatGradeIdCountMap: Map<UUID, Int>
        get() =
            paymentItems.groupBy { it.seatGradeId }.mapValues {
                it.value.sumOf { item -> item.reservationCount }
            }
}
