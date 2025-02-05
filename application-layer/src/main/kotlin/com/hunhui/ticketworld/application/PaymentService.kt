package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.PaymentCompleteRequest
import com.hunhui.ticketworld.application.dto.request.PaymentStartRequest
import com.hunhui.ticketworld.application.dto.response.PaymentStartResponse
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentItem
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.ROUND_NOT_AVAILABLE
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.EXPIRED
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

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

        val seatGrades: List<SeatGrade> = seatGradeRepository.findAllByPerformanceId(reservation.performanceId)

        // 각 결제 항목 생성: 할인 적용 로직은 Discount.apply() 사용
        val paymentItems: List<PaymentItem> =
            request.paymentItems.map { item ->
                val seatGrade: SeatGrade = seatGrades.first { it.id == item.seatGradeId }
                val paymentAmount: Money = seatGrade.calculatePaymentAmount(item.discountId, item.reservationCount)
                PaymentItem(
                    id = UUID.randomUUID(),
                    seatGradeId = item.seatGradeId,
                    reservationCount = item.reservationCount,
                    discountId = item.discountId,
                    paymentAmount = paymentAmount,
                )
            }

        // 결제 생성
        val payment: Payment =
            Payment.create(
                userId = request.userId,
                paymentMethod = request.paymentMethod,
                paymentItems = paymentItems,
            )
        paymentRepository.save(payment)
        return PaymentStartResponse(paymentId = payment.id, totalAmount = payment.totalAmount.amount)
    }

    @Transactional
    fun completePayment(request: PaymentCompleteRequest) {
        // TODO: 외부 결제 서버에 데이터 검증 요청

        // 예매가 만료되지 않았는지 확인
        val reservation: Reservation = reservationRepository.getById(request.reservationId)
        if (reservation.isExpired) throw BusinessException(EXPIRED)

        // 예매 기간이 지난 회차가 아닌지 확인
        val performance = performanceRepository.getById(reservation.performanceId)
        if (!performance.isAvailableRoundId(reservation.roundId)) throw BusinessException(ROUND_NOT_AVAILABLE)

        // 예매 확정 처리
        val confirmedReservation = reservation.confirm(request.userId, request.paymentId)
        reservationRepository.save(confirmedReservation)

        // 결제 완료 처리
        val completedPayment = paymentRepository.getById(request.paymentId).complete()
        paymentRepository.save(completedPayment)
    }
}
