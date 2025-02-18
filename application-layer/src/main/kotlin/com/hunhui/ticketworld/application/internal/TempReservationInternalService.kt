package com.hunhui.ticketworld.application.internal

import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.TempReserveResponse
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.ROUND_NOT_AVAILABLE
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.RESERVATION_COUNT_EXCEED
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TempReservationInternalService(
    private val performanceRepository: PerformanceRepository,
    private val reservationRepository: ReservationRepository,
) {
    @Transactional
    internal fun tryTempReserve(request: TempReserveRequest): TempReserveResponse {
        // 예매 가능한 회차인지 확인
        val performance: Performance = performanceRepository.getByIdAndRoundId(request.performanceId, request.roundId)
        if (!performance.isAvailableRoundId(request.roundId)) throw BusinessException(ROUND_NOT_AVAILABLE)

        // 예매 가능한 수량인지 확인
        if (performance.maxReservationCount < request.ticketIds.size) throw BusinessException(RESERVATION_COUNT_EXCEED)

        // 예매할 티켓들과 유저 id로 임시 예매 생성
        val tickets: List<Ticket> = reservationRepository.getTicketsByIds(request.ticketIds)
        val reservation = Reservation.createTempReservation(tickets, request.userId, request.performanceId)
        reservationRepository.save(reservation)

        return TempReserveResponse(reservationId = reservation.id)
    }
}
