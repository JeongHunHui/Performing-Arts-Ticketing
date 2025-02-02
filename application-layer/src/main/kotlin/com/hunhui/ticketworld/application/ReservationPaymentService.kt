package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.PaymentInfoRequest
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.discount.Discount
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentInfo
import com.hunhui.ticketworld.domain.payment.PaymentMethod
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import java.util.UUID

class ReservationPaymentService(
    private val discounts: List<Discount>,
    private val reservation: Reservation,
    private val paymentInfos: List<PaymentInfoRequest>,
) {
    init {
        if (isPriceCountDifferent) throw BusinessException(ReservationErrorCode.INVALID_RESERVE_REQUEST)
    }

    fun pay(paymentMethod: PaymentMethod): Payment {
        val paymentInfos: List<PaymentInfo> = paymentInfos.map { it.toDomain() }

        return Payment(
            id = UUID.randomUUID(),
            userId = reservation.tryReserveUserId,
            paymentMethod = paymentMethod,
            paymentInfos = paymentInfos,
        )
    }

    private fun PaymentInfoRequest.toDomain(): PaymentInfo {
        val discount: Discount = getDiscount(discountId)
        val price: Money = reservation.getPriceById(performancePriceId)
        return PaymentInfo(
            id = UUID.randomUUID(),
            performancePriceId = performancePriceId,
            reservationCount = reservationCount,
            discountId = discountId,
            paymentAmount =
                discount.apply(
                    roundId = reservation.roundId,
                    priceId = performancePriceId,
                    price = price,
                    count = reservationCount,
                ),
        )
    }

    private val isPriceCountDifferent: Boolean
        get() = reservation.priceIdCountMap != paymentInfos.priceCount

    private val List<PaymentInfoRequest>.priceCount: Map<UUID, Int>
        get() =
            groupBy { it.performancePriceId }.mapValues {
                it.value.sumOf { paymentInfo -> paymentInfo.reservationCount }
            }

    private fun getDiscount(discountId: UUID?): Discount = discounts.find { it.id == discountId } ?: Discount.DEFAULT
}
