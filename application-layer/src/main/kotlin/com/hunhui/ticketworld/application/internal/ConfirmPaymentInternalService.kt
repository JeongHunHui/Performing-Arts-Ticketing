package com.hunhui.ticketworld.application.internal

import com.hunhui.ticketworld.application.dto.request.LockMode
import com.hunhui.ticketworld.application.dto.request.PaymentCompleteRequest
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.ROUND_NOT_AVAILABLE
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.RESERVATION_COUNT_EXCEED
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConfirmPaymentInternalService(
    private val performanceRepository: PerformanceRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository,
) {
    @Transactional
    internal fun completePayment(request: PaymentCompleteRequest) {
        // TODO: 외부 결제 서버에 데이터 검증 요청

        // 예매 조회
        val reservation: Reservation =
            when (request.selectReservationLockMode) {
                LockMode.PESSIMISTIC -> reservationRepository.getByIdWithPessimistic(request.reservationId)
                LockMode.OPTIMISTIC -> reservationRepository.getById(request.reservationId)
            }

        // 예매 가능한 회차인지 확인
        val performance = performanceRepository.getByIdAndRoundId(reservation.performanceId, reservation.roundId)
        if (!performance.isAvailableRoundId(reservation.roundId)) throw BusinessException(ROUND_NOT_AVAILABLE)

        // 예매 가능한 수량인지 확인
        val paidTicketCount: Int =
            when (request.selectTicketsLockMode) {
                LockMode.PESSIMISTIC ->
                    reservationRepository.getPaidTicketsByRoundIdAndUserIdWithPessimistic(
                        reservation.roundId,
                        request.userId,
                    )
                else -> reservationRepository.getPaidTicketsByRoundIdAndUserId(reservation.roundId, request.userId)
            }.size
        val isReservationCountExceed = performance.maxReservationCount < reservation.tickets.size + paidTicketCount
        if (isReservationCountExceed) throw BusinessException(RESERVATION_COUNT_EXCEED)

        // 예매 확정 처리
        val confirmedReservation = reservation.confirm(request.userId, request.paymentId)
        // 결제 완료 처리
        val completedPayment = paymentRepository.getById(request.paymentId).complete()

        reservationRepository.save(confirmedReservation)
        paymentRepository.save(completedPayment)
    }
}
