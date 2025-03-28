package com.hunhui.ticketworld.infra.jpa.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Embeddable
class ReservationStatisticsId(
    @Column(name = "performance_id", updatable = false, nullable = false)
    val performanceId: UUID,
    @Column(name = "standard_time", updatable = false, nullable = false)
    val standardTime: LocalDateTime,
) : Serializable
