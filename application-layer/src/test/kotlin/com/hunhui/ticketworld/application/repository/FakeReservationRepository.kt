package com.hunhui.ticketworld.application.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import java.util.UUID

class FakeReservationRepository : ReservationRepository {
    // 테스트용 in-memory 티켓 목록
    private val tickets = mutableMapOf<UUID, Ticket>()
    private val reservations = mutableMapOf<UUID, Reservation>()

    fun addTicket(ticket: Ticket) {
        tickets[ticket.id] = ticket
    }

    fun isExists(reservationId: UUID): Boolean = reservations[reservationId] != null

    override fun findTicketsByRoundIdAndAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<Ticket> =
        tickets
            .filter { (_, ticket) ->
                ticket.performanceRoundId == performanceRoundId && ticket.seatAreaId == seatAreaId
            }.values
            .toList()

    override fun getById(id: UUID): Reservation = reservations[id] ?: throw BusinessException(ReservationErrorCode.NOT_FOUND)

    override fun getTicketsByIds(ids: List<UUID>): List<Ticket> =
        tickets
            .filter { (id, _) ->
                id in ids
            }.values
            .toList()

    override fun save(reservation: Reservation) {
        reservations[reservation.id] = reservation
    }

    override fun saveNewTickets(tickets: List<Ticket>) {
        TODO("Not yet implemented")
    }
}
