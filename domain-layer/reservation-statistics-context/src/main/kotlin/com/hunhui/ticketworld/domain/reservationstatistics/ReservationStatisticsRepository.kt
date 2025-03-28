package com.hunhui.ticketworld.domain.reservationstatistics

import java.time.LocalDateTime

interface ReservationStatisticsRepository {
    fun saveAll(reservationStatisticsList: List<ReservationStatistics>)

    fun findPopularReservationStatistics(
        startTime: LocalDateTime,
        standardTime: LocalDateTime,
        size: Int,
    ): List<PopularReservationStatistics>
}
