package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
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
    @Column(name = "performance_id", nullable = false)
    val performanceId: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(name = "payment_id", nullable = true)
    val paymentId: UUID?,
    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    @JoinColumn(name = "reservation_id")
    val tickets: List<TicketEntity> = mutableListOf(),
    @Column(name = "date", nullable = true)
    val date: LocalDateTime?,
) : BaseTimeEntity()
