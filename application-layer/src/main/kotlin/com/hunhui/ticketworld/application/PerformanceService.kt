package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.PerformanceCreateRequest
import com.hunhui.ticketworld.application.dto.response.PerformanceCreateResponse
import com.hunhui.ticketworld.application.dto.response.PerformanceResponse
import com.hunhui.ticketworld.application.dto.response.PerformanceSummaryListResponse
import com.hunhui.ticketworld.application.dto.response.SeatAreasResponse
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.seat.SeatArea
import com.hunhui.ticketworld.domain.seat.SeatAreaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PerformanceService(
    private val performanceRepository: PerformanceRepository,
    private val seatAreaRepository: SeatAreaRepository,
    private val reservationRepository: ReservationRepository,
) {
    fun getPerformance(performanceId: UUID): PerformanceResponse = PerformanceResponse.from(performanceRepository.getById(performanceId))

    fun getPerformances(
        page: Int,
        size: Int,
    ): PerformanceSummaryListResponse {
        val performances = performanceRepository.findAll(page, size)
        return PerformanceSummaryListResponse.from(performances)
    }

    @Transactional
    fun createPerformance(performanceCreateRequest: PerformanceCreateRequest): PerformanceCreateResponse {
        val (performance, seatAreas) = performanceCreateRequest.toDomain()
        performanceRepository.save(performance)
        seatAreaRepository.saveAll(seatAreas)
        val performanceRounds: List<PerformanceRound> = performance.rounds
        val tickets =
            seatAreas.flatMap { seatArea ->
                seatArea.seats.flatMap { seat ->
                    performanceRounds.map { round ->
                        Ticket.create(
                            roundId = round.id,
                            areaId = seatArea.id,
                            seatId = seat.id,
                            priceId = seat.performancePriceId,
                            price = performance.getPriceById(seat.performancePriceId),
                        )
                    }
                }
            }
        reservationRepository.saveNewTickets(tickets)
        return PerformanceCreateResponse(performance.id)
    }

    fun getSeatAreas(performanceId: UUID): SeatAreasResponse {
        val seatAreas: List<SeatArea> = seatAreaRepository.findByPerformanceId(performanceId)
        return SeatAreasResponse.from(seatAreas)
    }
}
