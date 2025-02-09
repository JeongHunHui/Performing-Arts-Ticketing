package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "kopis_performance_facility")
internal class KopisPerformanceFacilityEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: String,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "address", nullable = false)
    val address: String,
    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    @JoinColumn(name = "kopis_performance_facility_id")
    val places: List<KopisPerformancePlaceEntity> = mutableListOf(),
) : BaseTimeEntity()
