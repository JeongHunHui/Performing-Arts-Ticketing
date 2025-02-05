package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.PaymentCompleteRequest
import com.hunhui.ticketworld.application.dto.request.PaymentStartRequest
import com.hunhui.ticketworld.application.dto.response.PaymentStartResponse
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.ROUND_NOT_AVAILABLE
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val performanceRepository: PerformanceRepository,
    private val reservationRepository: ReservationRepository,
    private val seatGradeRepository: SeatGradeRepository,
    private val paymentRepository: PaymentRepository,
) {
    @Transactional
    fun startPayment(request: PaymentStartRequest): PaymentStartResponse {
        val reservation: Reservation = reservationRepository.getById(request.reservationId)
        // 결제 요청의 좌석 등급과 수량이 예매와 일치하는지 확인
        request.checkSeatGradeCountByReservation(reservation)

        // 공연의 좌석 등급 목록 조회
        val seatGrades: List<SeatGrade> = seatGradeRepository.findAllByPerformanceId(reservation.performanceId)

        // 결제 금액 계산 및 저장
        val payment = Payment.create(userId = request.userId, paymentMethod = request.paymentMethod)
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

    @Transactional
    fun completePayment(request: PaymentCompleteRequest) {
        // TODO: 외부 결제 서버에 데이터 검증 요청

        // 예매 조회
        val reservation: Reservation = reservationRepository.getById(request.reservationId)

        // 예매 기간이 지난 회차가 아닌지 확인
        val performance = performanceRepository.getById(reservation.performanceId)
        if (!performance.isAvailableRoundId(reservation.roundId)) throw BusinessException(ROUND_NOT_AVAILABLE)

        // 예매 확정 처리
        val confirmedReservation = reservation.confirm(request.userId, request.paymentId)
        // 결제 완료 처리
        val completedPayment = paymentRepository.getById(request.paymentId).complete()

        reservationRepository.save(confirmedReservation)
        paymentRepository.save(completedPayment)
    }
}
