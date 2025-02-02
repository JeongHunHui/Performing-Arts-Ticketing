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
    @Column(name = "seatId", nullable = false)
    val seatId: UUID,
    @Column(name = "performancePriceId", nullable = false)
    val performancePriceId: UUID,
    @Column(name = "price", nullable = false)
    val price: Long,
    @Column(name = "userId", nullable = true)
    val userId: UUID?,
    @Column(name = "paymentId", nullable = true)
    val paymentId: UUID?,
    @Column(name = "reservationExpireTime", nullable = true)
    val reservationExpireTime: LocalDateTime,
) : BaseTimeEntity()
