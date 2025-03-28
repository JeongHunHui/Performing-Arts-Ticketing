package com.hunhui.ticketworld.batch

import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceSummary
import com.hunhui.ticketworld.domain.performance.PopularityOption
import com.hunhui.ticketworld.domain.reservationstatistics.PopularReservationStatistics
import com.hunhui.ticketworld.domain.reservationstatistics.ReservationStatisticsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class CachePopularPerformanceServiceTest {
    private val reservationStatisticsRepository: ReservationStatisticsRepository = mockk()
    private val performanceRepository: PerformanceRepository = mockk(relaxed = true)
    private val service = CachePopularPerformanceService(reservationStatisticsRepository, performanceRepository)

    @Test
    fun `cachePopularPerformances should build PopularPerformanceSummaries and save via performanceRepository`() {
        val startTime = LocalDateTime.of(2025, 3, 26, 0, 0)
        val standardTime = LocalDateTime.of(2025, 3, 26, 11, 30)

        val popularStats =
            listOf(
                PopularReservationStatistics(
                    performanceId = "00000000-0000-0000-0000-000000000001",
                    reservationRate = BigDecimal.valueOf(80.0),
                ),
                PopularReservationStatistics(
                    performanceId = "00000000-0000-0000-0000-000000000002",
                    reservationRate = BigDecimal.valueOf(70.0),
                ),
            )
        every { reservationStatisticsRepository.findPopularReservationStatistics(startTime, standardTime, 50) } returns popularStats

        val performanceSummaries =
            listOf(
                PerformanceSummary(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    title = "Performance A",
                    genre = PerformanceGenre.CONCERT,
                    startDate = LocalDate.of(2025, 3, 1),
                    finishDate = LocalDate.of(2025, 3, 31),
                    posterUrl = "http://example.com/a.jpg",
                    location = "Location A",
                ),
                PerformanceSummary(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    title = "Performance B",
                    genre = PerformanceGenre.CONCERT,
                    startDate = LocalDate.of(2025, 3, 5),
                    finishDate = LocalDate.of(2025, 3, 25),
                    posterUrl = "http://example.com/b.jpg",
                    location = "Location B",
                ),
            )
        every { performanceRepository.findAllPerformanceSummariesByIds(any()) } returns performanceSummaries

        service.cachePopularPerformances(startTime, standardTime)

        verify {
            performanceRepository.savePopularPerformanceSummaries(
                PopularityOption.DAILY,
                match { summaries ->
                    summaries.standardTime == standardTime &&
                        summaries.performances.size == 2 &&
                        summaries.performances.first().reservationRate == BigDecimal.valueOf(80.0) &&
                        summaries.performances.last().reservationRate == BigDecimal.valueOf(70.0)
                },
            )
        }
    }
}
