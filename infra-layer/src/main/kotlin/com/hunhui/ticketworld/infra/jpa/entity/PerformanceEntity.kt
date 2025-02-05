package com.hunhui.ticketworld.infra.jpa.entity

import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "performance")
internal class PerformanceEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "title", nullable = false)
    val title: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    val genre: PerformanceGenre,
    @Column(name = "imageUrl", nullable = false)
    val imageUrl: String,
    @Column(name = "location", nullable = false)
    val location: String,
    @Column(name = "description", nullable = false)
    val description: String,
    @Column(name = "maxReservationCount", nullable = false)
    val maxReservationCount: Int,
    @OneToMany(
        mappedBy = "performanceId",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val seatGrades: List<SeatGradeEntity> = emptyList(),
    @OneToMany(
        mappedBy = "performanceId",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val rounds: List<PerformanceRoundEntity> = emptyList(),
) : BaseTimeEntity()
