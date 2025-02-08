package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "kopis_performance_place")
internal class KopisPerformancePlaceEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: String,
    @Column(name = "kopis_performance_facility_id")
    val kopisPerformanceFacilityId: String,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "seat_scale", nullable = false)
    val seatScale: Int?,
) : BaseTimeEntity()
