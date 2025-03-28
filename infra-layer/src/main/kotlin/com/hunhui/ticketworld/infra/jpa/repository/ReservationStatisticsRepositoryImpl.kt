package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.domain.reservationstatistics.PopularReservationStatistics
import com.hunhui.ticketworld.domain.reservationstatistics.ReservationStatistics
import com.hunhui.ticketworld.domain.reservationstatistics.ReservationStatisticsRepository
import com.hunhui.ticketworld.infra.jpa.entity.ReservationStatisticsEntity
import com.hunhui.ticketworld.infra.jpa.entity.id.ReservationStatisticsId
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
internal class ReservationStatisticsRepositoryImpl(
    private val reservationStatisticsJpaRepository: ReservationStatisticsJpaRepository,
) : ReservationStatisticsRepository {
    override fun saveAll(reservationStatisticsList: List<ReservationStatistics>) {
        reservationStatisticsJpaRepository.saveAll(reservationStatisticsList.map { it.entity })
    }

    override fun findPopularReservationStatistics(
        startTime: LocalDateTime,
        standardTime: LocalDateTime,
        size: Int,
    ): List<PopularReservationStatistics> =
        reservationStatisticsJpaRepository.findPopularReservationStatistics(startTime, standardTime, size)

    private val ReservationStatistics.entity: ReservationStatisticsEntity
        get() =
            ReservationStatisticsEntity(
                id = ReservationStatisticsId(performanceId, standardTime),
                count = count,
            )
}
