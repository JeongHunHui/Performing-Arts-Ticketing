package com.hunhui.ticketworld.domain.performance

import java.time.LocalDate
import java.util.UUID

class PerformanceSummary(
    val id: UUID,
    val title: String,
    val genre: PerformanceGenre,
    val startDate: LocalDate,
    val finishDate: LocalDate,
    val posterUrl: String?,
    val location: String,
)
