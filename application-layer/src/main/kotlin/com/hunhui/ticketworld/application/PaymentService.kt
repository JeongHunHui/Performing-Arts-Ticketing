package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.CompletePaymentRequest
import com.hunhui.ticketworld.application.dto.request.PaymentRequestInfo
import com.hunhui.ticketworld.application.dto.request.StartPaymentRequest
import com.hunhui.ticketworld.application.dto.response.StartPaymentResponse
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.discount.Discount
import com.hunhui.ticketworld.domain.discount.DiscountRepository
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentInfo
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.Performance
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
    fun startPayment(startPaymentRequest: StartPaymentRequest): StartPaymentResponse {
        // 예매가 만료되었으면 예외 발생
        val reservation: Reservation = reservationRepository.getById(startPaymentRequest.reservationId)
        if (reservation.isExpired) throw BusinessException(ReservationErrorCode.EXPIRED)

        // reservation으로 startPaymentRequest 검증
        val isPriceCountDifferent: Boolean =
            reservation.priceIdCountMap != startPaymentRequest.paymentRequestInfos.priceCount
        if (isPriceCountDifferent) throw BusinessException(ReservationErrorCode.INVALID_RESERVE_REQUEST)

        // 할인 목록 조회 및 유효성 검증
        val discounts: List<Discount> = discountRepository.findAllByIds(startPaymentRequest.getDiscountIds())
        val isInvalidDiscount: Boolean = discounts.any { it.performanceId != reservation.performanceId }
        if (isInvalidDiscount) throw BusinessException(ReservationErrorCode.INVALID_RESERVE_REQUEST)

        // 결제 요청 정보들을 통해 할인을 적용한 결제 금액 계산 및 결제 정보 생성
        val paymentInfos: List<PaymentInfo> =
            startPaymentRequest.paymentRequestInfos.map {
                val discount: Discount = discounts.getById(it.discountId)
                val price: Money = reservation.getPriceById(it.performancePriceId)
                PaymentInfo(
                    id = UUID.randomUUID(),
                    performancePriceId = it.performancePriceId,
                    reservationCount = it.reservationCount,
                    discountId = it.discountId,
                    paymentAmount =
                        discount.apply(
                            roundId = reservation.roundId,
                            priceId = it.performancePriceId,
                            price = price,
                            count = it.reservationCount,
                        ),
                )
            }

        // 결제 생성 및 저장
        val payment =
            Payment.create(
                userId = startPaymentRequest.userId,
                paymentMethod = startPaymentRequest.paymentMethod,
                paymentInfos = paymentInfos,
            )
        paymentRepository.save(payment)
        return StartPaymentResponse(paymentId = payment.id, totalAmount = payment.totalAmount.amount)
    }

    @Transactional
    fun completePayment(completePaymentRequest: CompletePaymentRequest) {
        // TODO: 토스 결제 서버에 데이터 검증 요청

        // 예매 조회
        val reservation: Reservation = reservationRepository.getById(completePaymentRequest.reservationId)

        // 예매 가능한 회차인지 확인
        val performance: Performance = performanceRepository.getById(reservation.performanceId)
        val isNotAvailableRound: Boolean = !performance.isAvailableRoundId(reservation.roundId)
        if (isNotAvailableRound) throw BusinessException(ReservationErrorCode.ROUND_NOT_AVAILABLE)

        // 예매 확정 및 업데이트
        val confirmedReservation: Reservation =
            reservation.confirm(
                userId = completePaymentRequest.userId,
                paymentId = completePaymentRequest.paymentId,
            )
        reservationRepository.save(confirmedReservation)

        // 결제 완료 및 업데이트
        val payment: Payment = paymentRepository.getById(completePaymentRequest.paymentId)
        val completedPayment: Payment = payment.complete()
        paymentRepository.save(completedPayment)
    }

    private val List<PaymentRequestInfo>.priceCount: Map<UUID, Int>
        get() =
            groupBy { it.performancePriceId }.mapValues {
                it.value.sumOf { paymentInfo -> paymentInfo.reservationCount }
            }

    private fun List<Discount>.getById(discountId: UUID?): Discount = find { it.id == discountId } ?: Discount.DEFAULT
}
