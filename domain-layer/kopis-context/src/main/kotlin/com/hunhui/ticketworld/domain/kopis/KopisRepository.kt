package com.hunhui.ticketworld.domain.kopis

import java.time.LocalDate

interface KopisRepository {
    fun findPerformanceIds(
        currentPage: Int,
        rows: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        openRun: Boolean,
        kopisPerformanceGenre: KopisPerformanceGenre,
    ): List<String>

    fun getPerformanceById(id: String): KopisPerformance

    fun getPerformanceFacilityById(id: String): KopisPerformanceFacility
}
