package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import com.hunhui.ticketworld.infra.jpa.entity.ReservationEntity
import com.hunhui.ticketworld.infra.jpa.entity.TicketEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class ReservationRepositoryImpl(
    private val ticketJpaRepository: TicketJpaRepository,
    private val reservationJpaRepository: ReservationJpaRepository,
) : ReservationRepository {
    override fun getById(id: UUID): Reservation =
        reservationJpaRepository.findByIdOrNull(id)?.domain ?: throw BusinessException(ReservationErrorCode.NOT_FOUND)

    override fun getTicketsByIds(ids: List<UUID>): List<Ticket> = ticketJpaRepository.findAllById(ids).map { it.domain }

    override fun findTicketsByRoundIdAndAreaId(
        performanceRoundId: UUID,
        seatAreaId: UUID,
    ): List<Ticket> = ticketJpaRepository.findAllByPerformanceRoundIdAndSeatAreaId(performanceRoundId, seatAreaId).map { it.domain }

    override fun save(reservation: Reservation) {
        reservationJpaRepository.save(reservation.entity)
    }

    override fun saveNewTickets(tickets: List<Ticket>) {
        val ticketEntities: List<TicketEntity> = tickets.map { it.entity }
        ticketJpaRepository.saveAll(ticketEntities)
    }

    private val ReservationEntity.domain: Reservation
        get() {
            return Reservation(
                id = id,
                performanceId = performanceId,
                userId = userId,
                paymentId = paymentId,
                tickets = tickets.map { it.domain },
                reservedAt = reservedAt,
            )
        }

    private val TicketEntity.domain: Ticket
        get() =
            Ticket(
                id = id,
                roundId = performanceRoundId,
                seatAreaId = seatAreaId,
                seatId = seatId,
                performancePriceId = performancePriceId,
                reservationId = reservationId,
                price = Money(price),
                isPaid = isPaid,
                expireTime = expireTime,
            )

    private val Reservation.entity: ReservationEntity
        get() =
            ReservationEntity(
                id = id,
                performanceId = performanceId,
                userId = userId,
                paymentId = paymentId,
                tickets = tickets.map { it.entity },
                reservedAt = reservedAt,
            )

    private val Ticket.entity: TicketEntity
        get() =
            TicketEntity(
                id = id,
                performanceRoundId = roundId,
                seatAreaId = seatAreaId,
                seatId = seatId,
                performancePriceId = performancePriceId,
                reservationId = reservationId,
                price = price.amount,
                isPaid = isPaid,
                expireTime = expireTime,
            )
}
