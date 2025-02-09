package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "performance_round")
internal class PerformanceRoundEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "round_start_time", nullable = false)
    val roundStartTime: LocalDateTime,
    @Column(name = "reservation_start_time", nullable = false)
    val reservationStartTime: LocalDateTime,
    @Column(name = "reservation_end_time", nullable = false)
    val reservationEndTime: LocalDateTime,
    @Column(name = "performance_id", nullable = false)
    val performanceId: UUID,
    @Column(name = "is_ticket_created", nullable = false)
    val isTicketCreated: Boolean = false,
) : BaseTimeEntity()
