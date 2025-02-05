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
import com.hunhui.ticketworld.domain.seatarea.SeatArea
import com.hunhui.ticketworld.domain.seatarea.SeatAreaRepository
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PerformanceService(
    private val performanceRepository: PerformanceRepository,
    private val seatGradeRepository: SeatGradeRepository,
    private val seatAreaRepository: SeatAreaRepository,
    private val reservationRepository: ReservationRepository,
) {
    @Transactional(readOnly = true)
    fun getPerformance(performanceId: UUID): PerformanceResponse =
        PerformanceResponse.from(
            performance = performanceRepository.getById(performanceId),
            seatGrades = seatGradeRepository.findAllByPerformanceId(performanceId),
        )

    fun getPerformanceSummaryList(
        page: Int,
        size: Int,
    ): PerformanceSummaryListResponse {
        val performances = performanceRepository.findAll(page, size)
        return PerformanceSummaryListResponse.from(performances)
    }

    @Transactional
    fun createPerformance(request: PerformanceCreateRequest): PerformanceCreateResponse {
        val (performance, seatGrades, seatAreas) = request.toDomain()
        performanceRepository.save(performance)
        seatGradeRepository.saveAll(seatGrades)
        seatAreaRepository.saveAll(seatAreas)
        val performanceRounds: List<PerformanceRound> = performance.rounds
        val tickets =
            seatAreas.flatMap { seatArea ->
                seatArea.positions.flatMap { seat ->
                    performanceRounds.map { round ->
                        Ticket.create(
                            performanceRoundId = round.id,
                            seatAreaId = seatArea.id,
                            seatPositionId = seat.id,
                            seatGradeId = seat.seatGradeId,
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
