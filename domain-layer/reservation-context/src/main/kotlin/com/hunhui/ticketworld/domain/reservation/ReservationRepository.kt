package com.hunhui.ticketworld.domain.reservation

import java.util.UUID

interface ReservationRepository {
    fun getById(id: UUID): Reservation

    fun getByIdWithPessimistic(id: UUID): Reservation

    fun getTicketsByIds(ids: List<UUID>): List<Ticket>

    fun getTicketsByIdsWithPessimistic(ids: List<UUID>): List<Ticket>

    fun findTicketsByRoundIdAndAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<Ticket>

    fun save(reservation: Reservation)

    fun saveNewTickets(tickets: List<Ticket>)

    fun saveAll(reservations: List<Reservation>)

    fun getPaidTicketsByRoundIdAndUserId(
        roundId: UUID,
        userId: UUID,
    ): List<Ticket>

    fun getPaidTicketsByRoundIdAndUserIdWithPessimistic(
        roundId: UUID,
        userId: UUID,
    ): List<Ticket>
}
