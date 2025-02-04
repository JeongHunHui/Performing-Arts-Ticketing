package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "reservation")
internal class ReservationEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "performanceId", nullable = false)
    val performanceId: UUID,
    @Column(name = "userId", nullable = false)
    val userId: UUID,
    @Column(name = "paymentId", nullable = true)
    val paymentId: UUID?,
    @OneToMany(mappedBy = "reservationId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val tickets: List<TicketEntity> = mutableListOf(),
    @Column(name = "reservedAt", nullable = true)
    val reservedAt: LocalDateTime?,
) : BaseTimeEntity()
