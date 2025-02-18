package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.TempReserveResponse
import com.hunhui.ticketworld.application.dto.response.TicketListResponse
import com.hunhui.ticketworld.application.internal.TempReservationInternalService
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.CANNOT_TEMP_RESERVE
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val tempReservationInternalService: TempReservationInternalService,
) {
    fun findAllTickets(
        roundId: UUID,
        areaId: UUID,
    ): TicketListResponse {
        val ticketList: List<Ticket> = reservationRepository.findTicketsByRoundIdAndAreaId(roundId, areaId)
        return TicketListResponse.from(ticketList)
    }

    fun tempReserve(request: TempReserveRequest): TempReserveResponse =
        try {
            tempReservationInternalService.tryTempReserve(request)
        } catch (e: ObjectOptimisticLockingFailureException) {
            throw BusinessException(CANNOT_TEMP_RESERVE)
        }
}
