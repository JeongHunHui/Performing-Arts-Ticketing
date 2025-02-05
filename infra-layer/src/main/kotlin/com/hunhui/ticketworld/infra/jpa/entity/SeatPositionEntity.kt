package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "seat_position")
internal class SeatPositionEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "seatAreaId", nullable = false)
    val seatAreaId: UUID,
    @Column(name = "seatGradeId", nullable = false)
    val seatGradeId: UUID,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "x", nullable = false)
    val x: Int,
    @Column(name = "y", nullable = false)
    val y: Int,
) : BaseTimeEntity()
