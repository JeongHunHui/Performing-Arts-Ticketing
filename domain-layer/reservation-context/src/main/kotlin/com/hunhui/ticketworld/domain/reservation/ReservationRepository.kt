package com.hunhui.ticketworld.domain.reservation

import java.util.UUID

interface ReservationRepository {
    fun getTicketById(id: UUID): Ticket

    fun getReservations(
        ids: List<UUID>,
        tryReserveUserId: UUID,
    ): Reservations

    fun findTicketsByRoundIdAndAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<Ticket>

    fun saveTicket(ticket: Ticket)

    fun saveTickets(tickets: List<Ticket>)

    fun saveAll(reservations: Reservations)
}
