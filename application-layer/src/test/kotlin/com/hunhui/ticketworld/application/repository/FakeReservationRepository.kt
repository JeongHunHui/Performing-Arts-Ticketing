package com.hunhui.ticketworld.application.repository

import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import java.util.UUID

class FakeReservationRepository : ReservationRepository {
    // 테스트용 in-memory 티켓 목록
    private val tickets = mutableListOf<Ticket>()
    private val reservations = mutableListOf<Reservation>()

    fun addTicket(ticket: Ticket) {
        tickets.add(ticket)
    }

    fun isExists(reservationId: UUID): Boolean = reservations.any { it.id == reservationId }

    override fun findTicketsByRoundIdAndAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<Ticket> =
        tickets.filter {
            it.performanceRoundId == performanceRoundId && it.seatAreaId == seatAreaId
        }

    override fun getById(id: UUID): Reservation {
        TODO("Not yet implemented")
    }

    override fun getTicketsByIds(ids: List<UUID>): List<Ticket> = tickets.filter { it.id in ids }

    override fun save(reservation: Reservation) {
        reservations.add(reservation)
    }

    override fun saveNewTickets(tickets: List<Ticket>) {
        TODO("Not yet implemented")
    }
}
