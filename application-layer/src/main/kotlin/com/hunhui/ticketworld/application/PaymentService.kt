package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.PaymentCompleteRequest
import com.hunhui.ticketworld.application.dto.request.PaymentStartRequest
import com.hunhui.ticketworld.application.dto.response.PaymentStartResponse
import com.hunhui.ticketworld.application.internal.ConfirmPaymentInternalService
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.payment.exception.PaymentErrorCode.CANNOT_COMPLETE
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val reservationRepository: ReservationRepository,
    private val seatGradeRepository: SeatGradeRepository,
    private val paymentRepository: PaymentRepository,
    private val confirmPaymentInternalService: ConfirmPaymentInternalService,
) {
    @Transactional
    fun startPayment(request: PaymentStartRequest): PaymentStartResponse {
        val reservation: Reservation = reservationRepository.getById(request.reservationId)
        // 결제 요청의 좌석 등급과 수량이 예매와 일치하는지 확인
        request.checkSeatGradeCountByReservation(reservation)

        // 공연의 좌석 등급 목록 조회
        val seatGrades: List<SeatGrade> = seatGradeRepository.findAllByPerformanceId(reservation.performanceId)

        // 결제 금액 계산 및 저장
        val payment =
            Payment.create(
                userId = request.userId,
                roundId = reservation.roundId,
                paymentMethod = request.paymentMethod,
            )

        for (item in request.paymentItems) {
            val seatGrade: SeatGrade = seatGrades.first { it.id == item.seatGradeId }

            // 좌석 등급, 예매 수량, 할인 ID를 통해 결제 항목의 금액 계산
            // discountId나 예매 수량이 잘못되었을 경우 예외 발생
            val (discountName, originalPrice, discountedPrice) = seatGrade.calculatePaymentAmount(item.discountId, item.reservationCount)
            payment.addItem(
                seatGradeName = seatGrade.name,
                reservationCount = item.reservationCount,
                discountName = discountName,
                originalPrice = originalPrice,
                discountedPrice = discountedPrice,
            )
        }
        paymentRepository.save(payment)

        return PaymentStartResponse(paymentId = payment.id, totalAmount = payment.totalAmount.amount)
    }

    fun completePayment(request: PaymentCompleteRequest) =
        try {
            confirmPaymentInternalService.completePayment(request)
        } catch (e: ObjectOptimisticLockingFailureException) {
            throw BusinessException(CANNOT_COMPLETE)
        }
}
