package com.hunhui.ticketworld.batch

import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceSummary
import com.hunhui.ticketworld.domain.performance.PopularPerformanceSummaries
import com.hunhui.ticketworld.domain.performance.PopularityOption
import com.hunhui.ticketworld.domain.reservationstatistics.PopularReservationStatistics
import com.hunhui.ticketworld.domain.reservationstatistics.ReservationStatisticsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CachePopularPerformanceService(
    private val reservationStatisticsRepository: ReservationStatisticsRepository,
    private val performanceRepository: PerformanceRepository,
) {
    companion object {
        private const val SIZE = 50
    }

    @Transactional(readOnly = true)
    fun cachePopularPerformances(
        startTime: LocalDateTime,
        standardTime: LocalDateTime,
    ) {
        val popularReservationStatisticsList: List<PopularReservationStatistics> =
            reservationStatisticsRepository.findPopularReservationStatistics(
                startTime,
                standardTime,
                SIZE,
            )

        val performanceIds = popularReservationStatisticsList.map { it.performanceId }

        val performanceSummaries: List<PerformanceSummary> = performanceRepository.findAllPerformanceSummariesByIds(performanceIds)

        val statsMap = popularReservationStatisticsList.associateBy { it.performanceId }

        val popularPerformanceSummaries =
            PopularPerformanceSummaries(
                standardTime = standardTime,
                performances =
                    performanceSummaries
                        .mapNotNull { performance ->
                            statsMap[performance.id]?.let { stats ->
                                PopularPerformanceSummaries.PopularPerformanceSummary(
                                    id = performance.id,
                                    title = performance.title,
                                    genre = performance.genre,
                                    startDate = performance.startDate,
                                    finishDate = performance.finishDate,
                                    posterUrl = performance.posterUrl,
                                    location = performance.location,
                                    reservationRate = stats.reservationRate,
                                )
                            }
                        }.sortedByDescending { it.reservationRate },
            )

        performanceRepository.savePopularPerformanceSummaries(PopularityOption.DAILY, popularPerformanceSummaries)
    }
}
