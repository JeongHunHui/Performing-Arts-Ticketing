package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "ticket")
internal class TicketEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "performanceRoundId", nullable = false)
    val performanceRoundId: UUID,
    @Column(name = "seatAreaId", nullable = false)
    val seatAreaId: UUID,
    @Column(name = "seatPositionId", nullable = false)
    val seatPositionId: UUID,
    @Column(name = "seatGradeId", nullable = false)
    val seatGradeId: UUID,
    @Column(name = "reservationId", nullable = true)
    val reservationId: UUID? = null,
    @Column(name = "isPaid", nullable = false)
    val isPaid: Boolean,
    @Column(name = "expireTime", nullable = false)
    val expireTime: LocalDateTime,
) : BaseTimeEntity()
