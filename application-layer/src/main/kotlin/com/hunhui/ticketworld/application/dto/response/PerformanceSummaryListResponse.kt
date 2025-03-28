package com.hunhui.ticketworld.application.dto.response

import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PerformanceSummary
import java.time.LocalDate
import java.util.UUID

data class PerformanceSummaryListResponse(
    val totalPages: Int,
    val performances: List<PerformanceSummaryResponse>,
) {
    companion object {
        fun of(
            totalPages: Int,
            performanceSummaries: List<PerformanceSummary>,
        ) = PerformanceSummaryListResponse(
            totalPages = totalPages,
            performances =
                performanceSummaries.map {
                    PerformanceSummaryResponse(
                        id = it.id,
                        title = it.title,
                        genre = it.genre,
                        startDate = it.startDate,
                        finishDate = it.finishDate,
                        posterUrl = it.posterUrl,
                        location = it.location,
                    )
                },
        )
    }

    data class PerformanceSummaryResponse(
        val id: UUID,
        val title: String,
        val genre: PerformanceGenre,
        val startDate: LocalDate,
        val finishDate: LocalDate,
        val posterUrl: String?,
        val location: String,
    )
}
