package com.hunhui.ticketworld.domain.performance

import java.util.UUID

interface PerformanceRepository {
    fun getById(id: UUID): Performance

    fun getByIdAndRoundId(
        performanceId: UUID,
        roundId: UUID,
    ): Performance

    fun findByKopisId(kopisId: String): Performance?

    fun findAllPerformanceSummaries(
        page: Int,
        size: Int,
        performanceSortOption: PerformanceSortOption,
        isAsc: Boolean,
    ): Pair<List<PerformanceSummary>, Int>

    fun getPopularPerformanceSummaries(popularityOption: PopularityOption): PopularPerformanceSummaries

    fun findAllWithPagenation(
        page: Int,
        size: Int,
    ): Pair<List<Performance>, Int>

    fun findAllPerformanceSummariesByIds(ids: List<UUID>): List<PerformanceSummary>

    fun save(performance: Performance)

    fun savePopularPerformanceSummaries(
        popularityOption: PopularityOption,
        popularPerformanceSummaries: PopularPerformanceSummaries,
    )
}
