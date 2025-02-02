package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import com.hunhui.ticketworld.infra.jpa.entity.TicketEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class ReservationRepositoryImpl(
    private val ticketJpaRepository: TicketJpaRepository,
) : ReservationRepository {
    override fun getTicketById(id: UUID): Ticket =
        ticketJpaRepository.findByIdOrNull(id)?.domain ?: throw BusinessException(ReservationErrorCode.CANNOT_RESERVE)

    override fun getByIds(ids: List<UUID>) =
        Reservation(
            tickets = ticketJpaRepository.findAllById(ids).map { it.domain },
        )

    override fun findTicketsByRoundIdAndAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<Ticket> =
        ticketJpaRepository
            .findAllByPerformanceRoundIdAndSeatAreaId(performanceRoundId, seatAreaId)
            .map { it.domain }

    override fun saveTicket(ticket: Ticket) {
        ticketJpaRepository.save(ticket.entity)
    }

    override fun saveTickets(tickets: List<Ticket>) {
        ticketJpaRepository.saveAll(tickets.map { it.entity })
    }

    override fun save(reservation: Reservation) {
        ticketJpaRepository.saveAll(reservation.tickets.map { it.entity })
    }

    private val Ticket.entity: TicketEntity
        get() =
            TicketEntity(
                id = id,
                performanceRoundId = roundId,
                seatAreaId = seatAreaId,
                seatId = seatId,
                performancePriceId = performancePriceId,
                price = price.amount,
                userId = tempUserId,
                paymentId = paymentId,
                reservationExpireTime = reservationExpireTime,
            )

    private val TicketEntity.domain: Ticket
        get() =
            Ticket(
                id = id,
                roundId = performanceRoundId,
                seatAreaId = seatAreaId,
                seatId = seatId,
                performancePriceId = performancePriceId,
                price = Money(price),
                tempUserId = userId,
                paymentId = paymentId,
                reservationExpireTime = reservationExpireTime,
            )
}
