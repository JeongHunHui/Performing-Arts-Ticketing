package com.hunhui.ticketworld.domain.performance

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class PopularPerformanceSummaries(
    val standardTime: LocalDateTime,
    val performances: List<PopularPerformanceSummary>,
) {
    data class PopularPerformanceSummary(
        val id: UUID,
        val title: String,
        val genre: PerformanceGenre,
        val startDate: LocalDate,
        val finishDate: LocalDate,
        val posterUrl: String?,
        val location: String,
        val reservationRate: BigDecimal,
    )
}
