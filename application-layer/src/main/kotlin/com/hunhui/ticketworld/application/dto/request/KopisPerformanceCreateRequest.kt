package com.hunhui.ticketworld.application.dto.request

import com.hunhui.ticketworld.domain.kopis.KopisPerformanceGenre
import java.time.LocalDate

data class KopisPerformanceCreateRequest(
    val currentPage: Int,
    val rows: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val openRun: Boolean,
    val kopisPerformanceGenre: KopisPerformanceGenre,
)
