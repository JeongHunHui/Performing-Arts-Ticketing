package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.CompletePaymentRequest
import com.hunhui.ticketworld.application.dto.request.PaymentRequestInfo
import com.hunhui.ticketworld.application.dto.request.StartPaymentRequest
import com.hunhui.ticketworld.application.dto.response.StartPaymentResponse
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.discount.Discount
import com.hunhui.ticketworld.domain.discount.DiscountRepository
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentInfo
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PaymentService(
    private val performanceRepository: PerformanceRepository,
    private val reservationRepository: ReservationRepository,
    private val discountRepository: DiscountRepository,
    private val paymentRepository: PaymentRepository,
) {
    @Transactional
    fun startPayment(request: StartPaymentRequest): StartPaymentResponse {
        val reservation: Reservation = reservationRepository.getById(request.reservationId)
        // 결제 요청과 예매한 티켓들의 가격과 수량이 일치하는지 확인
        request.validate(reservation)

        val discounts: List<Discount> = discountRepository.findAllByIds(request.getDiscountIds())
        // 할인의 공연 ID가 예매와 일치하는지 확인
        discounts.forEach {
            if (it.performanceId != reservation.performanceId) throw BusinessException(ReservationErrorCode.INVALID_RESERVE_REQUEST)
        }

        // 각 결제 항목 생성: 할인 적용 로직은 Discount.apply() 사용
        val paymentInfos: List<PaymentInfo> =
            request.paymentRequestInfos.map { info ->
                val discount: Discount = discounts.find { it.id == info.discountId } ?: Discount.DEFAULT
                val price = reservation.getPriceById(info.performancePriceId)
                val finalAmount =
                    discount.apply(
                        roundId = reservation.roundId,
                        priceId = info.performancePriceId,
                        price = price,
                        count = info.reservationCount,
                    )
                PaymentInfo(
                    id = UUID.randomUUID(),
                    performancePriceId = info.performancePriceId,
                    reservationCount = info.reservationCount,
                    discountId = info.discountId,
                    paymentAmount = finalAmount,
                )
            }

        // 결제 생성
        val payment: Payment =
            Payment.create(
                userId = request.userId,
                paymentMethod = request.paymentMethod,
                paymentInfos = paymentInfos,
            )
        paymentRepository.save(payment)
        return StartPaymentResponse(paymentId = payment.id, totalAmount = payment.totalAmount.amount)
    }

    @Transactional
    fun completePayment(request: CompletePaymentRequest) {
        // TODO: 외부 결제 서버에 데이터 검증 요청

        // 예매가 만료되지 않았는지 확인
        val reservation: Reservation = reservationRepository.getById(request.reservationId)
        if (reservation.isExpired) throw BusinessException(ReservationErrorCode.EXPIRED)

        // 예매 기간이 지난 회차가 아닌지 확인
        val performance = performanceRepository.getById(reservation.performanceId)
        if (!performance.isAvailableRoundId(reservation.roundId)) throw BusinessException(ReservationErrorCode.ROUND_NOT_AVAILABLE)

        // 예매 확정 처리
        val confirmedReservation = reservation.confirm(request.userId, request.paymentId)
        reservationRepository.save(confirmedReservation)

        // 결제 완료 처리
        val completedPayment = paymentRepository.getById(request.paymentId).complete()
        paymentRepository.save(completedPayment)
    }

    private fun StartPaymentRequest.validate(reservation: Reservation) {
        val isPriceCountDifferent: Boolean = reservation.priceIdCountMap != paymentRequestInfos.priceCount
        if (isPriceCountDifferent) throw BusinessException(ReservationErrorCode.INVALID_RESERVE_REQUEST)
    }

    private val List<PaymentRequestInfo>.priceCount: Map<UUID, Int>
        get() =
            groupBy { it.performancePriceId }.mapValues {
                it.value.sumOf { paymentInfo -> paymentInfo.reservationCount }
            }
}
