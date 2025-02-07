package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "seat_area")
internal class SeatAreaEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "performance_id", nullable = false)
    val performanceId: UUID,
    @Column(name = "width", nullable = false)
    val width: Int,
    @Column(name = "height", nullable = false)
    val height: Int,
    @Column(name = "floor_name", nullable = false)
    val floorName: String,
    @Column(name = "area_name", nullable = false)
    val areaName: String,
    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    @JoinColumn(name = "seat_area_id")
    val positions: List<SeatPositionEntity> = emptyList(),
) : BaseTimeEntity()
