package com.hunhui.ticketworld.infra.jpa.entity

import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "performance")
internal class PerformanceEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "kopis_id", nullable = true)
    val kopisId: String?,
    @Column(name = "kopis__facility_id", nullable = true)
    val kopisFacilityId: String?,
    @Column(name = "title", nullable = false)
    val title: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    val genre: PerformanceGenre,
    @Column(name = "poster_url", nullable = true)
    val posterUrl: String?,
    @Column(name = "location", nullable = false)
    val location: String,
    @Column(name = "location_address", nullable = true)
    val locationAddress: String?,
    @Column(name = "cast", nullable = true)
    val cast: String?,
    @Column(name = "crew", nullable = true)
    val crew: String?,
    @Column(name = "runtime", nullable = true)
    val runtime: String?,
    @Column(name = "age_limit", nullable = true)
    val ageLimit: String?,
    /** '|'로 URL 구분 */
    @Column(name = "description_image_urls", nullable = false, columnDefinition = "TEXT")
    val descriptionImageUrls: String,
    @Column(name = "description", nullable = false)
    val description: String,
    @Column(name = "max_reservation_count", nullable = false)
    val maxReservationCount: Int,
    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    @JoinColumn(name = "performance_id")
    val rounds: List<PerformanceRoundEntity> = emptyList(),
) : BaseTimeEntity()
