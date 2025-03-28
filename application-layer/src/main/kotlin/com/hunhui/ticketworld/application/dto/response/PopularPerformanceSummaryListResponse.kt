package com.hunhui.ticketworld.application.dto.response

import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PopularPerformanceSummaries
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class PopularPerformanceSummaryListResponse(
    val standardTime: LocalDateTime,
    val performances: List<PopularPerformanceSummaryResponse>,
) {
    companion object {
        fun from(popularPerformanceSummaries: PopularPerformanceSummaries) =
            PopularPerformanceSummaryListResponse(
                standardTime = popularPerformanceSummaries.standardTime,
                performances =
                    popularPerformanceSummaries.performances.map {
                        PopularPerformanceSummaryResponse(
                            id = it.id,
                            title = it.title,
                            genre = it.genre,
                            startDate = it.startDate,
                            finishDate = it.finishDate,
                            posterUrl = it.posterUrl,
                            location = it.location,
                            reservationRate = it.reservationRate,
                        )
                    },
            )
    }

    data class PopularPerformanceSummaryResponse(
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
