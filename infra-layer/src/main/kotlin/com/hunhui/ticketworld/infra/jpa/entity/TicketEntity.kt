package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "ticket")
internal class TicketEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "performance_round_id", nullable = false)
    val performanceRoundId: UUID,
    @Column(name = "seat_area_id", nullable = false)
    val seatAreaId: UUID,
    @Column(name = "seat_position_id", nullable = false)
    val seatPositionId: UUID,
    @Column(name = "seat_grade_id", nullable = false)
    val seatGradeId: UUID,
    @Column(name = "reservation_id", nullable = true)
    val reservationId: UUID? = null,
    @Column(name = "is_paid", nullable = false)
    val isPaid: Boolean,
    @Column(name = "expire_time", nullable = false)
    val expireTime: LocalDateTime,
    @Version
    @Column(name = "version")
    @ColumnDefault("0")
    val version: Long = 0L,
) : BaseTimeEntity()
