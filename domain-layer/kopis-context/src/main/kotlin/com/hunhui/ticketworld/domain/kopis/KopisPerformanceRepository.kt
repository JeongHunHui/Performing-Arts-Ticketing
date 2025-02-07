package com.hunhui.ticketworld.domain.kopis

import java.time.LocalDate

interface KopisPerformanceRepository {
    fun findIds(
        currentPage: Int,
        rows: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        openRun: Boolean,
        kopisPerformanceGenre: KopisPerformanceGenre,
    ): List<String>

    fun getById(id: String): KopisPerformance
}
