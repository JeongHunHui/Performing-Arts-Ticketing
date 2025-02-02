package com.hunhui.ticketworld.domain.reservation

import java.util.UUID

interface ReservationRepository {
    fun getTicketById(id: UUID): Ticket

    fun getByIds(ids: List<UUID>): Reservation

    fun findTicketsByRoundIdAndAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<Ticket>

    fun saveTicket(ticket: Ticket)

    fun saveTickets(tickets: List<Ticket>)

    fun save(reservation: Reservation)
}
