package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode.NOT_FOUND
import com.hunhui.ticketworld.infra.jpa.entity.ReservationEntity
import com.hunhui.ticketworld.infra.jpa.entity.TicketEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class ReservationRepositoryImpl(
    private val ticketJpaRepository: TicketJpaRepository,
    private val reservationJpaRepository: ReservationJpaRepository,
) : ReservationRepository {
    override fun getById(id: UUID): Reservation = reservationJpaRepository.findByIdOrNull(id)?.domain ?: throw BusinessException(NOT_FOUND)

    @Lock(LockModeType.OPTIMISTIC)
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

    override fun saveAll(reservations: List<Reservation>) {
        reservationJpaRepository.saveAll(reservations.map { it.entity })
    }

    override fun getPaidTicketCountByRoundIdAndUserId(
        roundId: UUID,
        userId: UUID,
    ): Int = reservationJpaRepository.getPaidTicketCountByRoundIdAndUserId(roundId, userId)

    private val ReservationEntity.domain: Reservation
        get() {
            return Reservation(
                id = id,
                performanceId = performanceId,
                userId = userId,
                paymentId = paymentId,
                tickets = tickets.map { it.domain },
                date = date,
            )
        }

    private val TicketEntity.domain: Ticket
        get() =
            Ticket(
                id = id,
                performanceRoundId = performanceRoundId,
                seatAreaId = seatAreaId,
                seatPositionId = seatPositionId,
                seatGradeId = seatGradeId,
                reservationId = reservationId,
                isPaid = isPaid,
                expireTime = expireTime,
                version = version,
            )

    private val Reservation.entity: ReservationEntity
        get() =
            ReservationEntity(
                id = id,
                performanceId = performanceId,
                userId = userId,
                paymentId = paymentId,
                tickets = tickets.map { it.entity },
                date = date,
            )

    private val Ticket.entity: TicketEntity
        get() =
            TicketEntity(
                id = id,
                performanceRoundId = performanceRoundId,
                seatAreaId = seatAreaId,
                seatPositionId = seatPositionId,
                seatGradeId = seatGradeId,
                reservationId = reservationId,
                isPaid = isPaid,
                expireTime = expireTime,
                version = version,
            )
}
