package com.hunhui.ticketworld.application.dto.response

import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import java.time.LocalDate
import java.util.UUID

data class PerformanceSummaryListResponse(
    val totalPages: Int,
    val performances: List<PerformanceSummaryResponse>,
) {
    companion object {
        fun of(
            performances: List<Performance>,
            totalPages: Int,
        ) = PerformanceSummaryListResponse(
            totalPages = totalPages,
            performances =
                performances.map {
                    PerformanceSummaryResponse(
                        id = it.id,
                        title = it.info.title,
                        genre = it.info.genre,
                        startDate = it.startDate,
                        finishDate = it.finishDate,
                        posterUrl = it.info.posterUrl,
                        location = it.info.location,
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
