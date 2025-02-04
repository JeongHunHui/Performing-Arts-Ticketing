package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.TempReserveResponse
import com.hunhui.ticketworld.application.dto.response.TicketListResponse
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReservationService(
    private val performanceRepository: PerformanceRepository,
    private val reservationRepository: ReservationRepository,
) {
    fun findAll(
        roundId: UUID,
        areaId: UUID,
    ): TicketListResponse {
        val ticketList: List<Ticket> = reservationRepository.findTicketsByRoundIdAndAreaId(roundId, areaId)
        return TicketListResponse.from(ticketList)
    }

    @Transactional
    fun tempReserve(tempReserveRequest: TempReserveRequest): TempReserveResponse {
        val performance: Performance = performanceRepository.getById(tempReserveRequest.performanceId)

        // 예매 가능 수량 확인
        // TODO: 회차 id와 유저 id를 이용하여 현재 예매 수량을 조회하도록 수정
        val currentReservationCount = 0
        val isReservationCountExceed: Boolean =
            performance.maxReservationCount < tempReserveRequest.ticketIds.size + currentReservationCount
        if (isReservationCountExceed) throw BusinessException(ReservationErrorCode.RESERVATION_COUNT_EXCEED)

        // 예매할 티켓들과 유저 id로 임시 예매 생성
        val tickets: List<Ticket> = reservationRepository.getTicketsByIds(tempReserveRequest.ticketIds)
        val reservation =
            Reservation.createTempReservation(
                tickets = tickets,
                userId = tempReserveRequest.userId,
                performanceId = tempReserveRequest.performanceId,
            )

        // 예매 가능한 회차인지 확인
        val isNotAvailableRound: Boolean = !performance.isAvailableRoundId(reservation.roundId)
        if (isNotAvailableRound) throw BusinessException(ReservationErrorCode.ROUND_NOT_AVAILABLE)

        reservationRepository.save(reservation)

        return TempReserveResponse(reservationId = reservation.id)
    }
}
