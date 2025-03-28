package com.hunhui.ticketworld.infra.jpa.entity

import com.hunhui.ticketworld.infra.jpa.entity.id.ReservationStatisticsId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "reservation_statistics")
class ReservationStatisticsEntity(
    @EmbeddedId
    val id: ReservationStatisticsId,
    @Column(name = "count", updatable = false, nullable = false)
    val count: Long,
)
