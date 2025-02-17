package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.TempReserveResponse
import com.hunhui.ticketworld.application.dto.response.TicketListResponse
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
import java.util.UUID

@Service
class ReservationService(
    private val performanceRepository: PerformanceRepository,
    private val reservationRepository: ReservationRepository,
) {
    fun findAllTickets(
        roundId: UUID,
        areaId: UUID,
    ): TicketListResponse {
        val ticketList: List<Ticket> = reservationRepository.findTicketsByRoundIdAndAreaId(roundId, areaId)
        return TicketListResponse.from(ticketList)
    }

    @Transactional
    fun tempReserve(request: TempReserveRequest): TempReserveResponse {
        // 예매할 티켓들과 유저 id로 임시 예매 생성
        val tickets: List<Ticket> = reservationRepository.getTicketsByIds(request.ticketIds)
        val reservation =
            Reservation.createTempReservation(
                tickets = tickets,
                userId = request.userId,
                performanceId = request.performanceId,
            )

        // 예매 가능한 회차인지 확인
        val performance: Performance = performanceRepository.getByIdAndRoundId(request.performanceId, reservation.roundId)
        if (!performance.isAvailableRoundId(reservation.roundId)) throw BusinessException(ROUND_NOT_AVAILABLE)

        // 예매 가능한 수량인지 확인
        val paidTicketCount: Int = reservationRepository.getPaidTicketCountByRoundIdAndUserId(reservation.roundId, request.userId)
        val isReservationCountExceed = performance.maxReservationCount < request.ticketIds.size + paidTicketCount
        if (isReservationCountExceed) throw BusinessException(RESERVATION_COUNT_EXCEED)

        reservationRepository.save(reservation)
        return TempReserveResponse(reservationId = reservation.id)
    }
}
