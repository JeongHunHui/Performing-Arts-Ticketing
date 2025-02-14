package com.hunhui.ticketworld.domain.reservation

import java.util.UUID

interface ReservationRepository {
    fun getById(id: UUID): Reservation

    fun getTicketsByIds(ids: List<UUID>): List<Ticket>

    fun findTicketsByRoundIdAndAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<Ticket>

    fun save(reservation: Reservation)

    fun saveNewTickets(tickets: List<Ticket>)

    fun saveAll(reservations: List<Reservation>)

    fun getPaidTicketCountByRoundIdAndUserId(
        roundId: UUID,
        userId: UUID,
    ): Int
}
