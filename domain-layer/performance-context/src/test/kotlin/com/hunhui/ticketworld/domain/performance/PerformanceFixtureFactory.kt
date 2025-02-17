package com.hunhui.ticketworld.domain.performance

import java.time.LocalDateTime

object PerformanceFixtureFactory {
    fun createValidPerformance(
        title: String = "테스트 공연",
        genre: PerformanceGenre = PerformanceGenre.CONCERT,
        posterUrl: String = "test_image.png",
        location: String = "테스트 장소",
        description: String = "테스트 공연 설명",
        maxReservationCount: Int = 5,
        rounds: List<PerformanceRound> =
            listOf(
                createValidPerformanceRound(),
                createValidPerformanceRound(
                    LocalDateTime.now().plusDays(2),
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusHours(1),
                ),
            ),
    ): Performance =
        Performance.create(
            performanceInfo =
                PerformanceInfo(
                    title = title,
                    genre = genre,
                    posterUrl = posterUrl,
                    location = location,
                    description = description,
                ),
            maxReservationCount = maxReservationCount,
            rounds = rounds,
        )

    private fun createValidPerformanceRound(
        roundStartTime: LocalDateTime = LocalDateTime.now().plusDays(3),
        reservationStartTime: LocalDateTime = LocalDateTime.now(),
        reservationEndTime: LocalDateTime = LocalDateTime.now().plusDays(1),
    ): PerformanceRound =
        PerformanceRound.create(
            roundStartTime,
            reservationStartTime,
            reservationEndTime,
        )
}
