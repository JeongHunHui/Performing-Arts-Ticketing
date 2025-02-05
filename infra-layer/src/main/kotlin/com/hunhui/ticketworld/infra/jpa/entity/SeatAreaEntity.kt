package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "seat_area")
internal class SeatAreaEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "performanceId", nullable = false)
    val performanceId: UUID,
    @Column(name = "width", nullable = false)
    val width: Int,
    @Column(name = "height", nullable = false)
    val height: Int,
    @Column(name = "floorName", nullable = false)
    val floorName: String,
    @Column(name = "areaName", nullable = false)
    val areaName: String,
    @OneToMany(
        mappedBy = "seatAreaId",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val positions: List<SeatPositionEntity> = emptyList(),
) : BaseTimeEntity()
