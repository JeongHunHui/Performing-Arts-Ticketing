package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.domain.reservationstatistics.PopularReservationStatistics
import com.hunhui.ticketworld.infra.jpa.entity.ReservationStatisticsEntity
import com.hunhui.ticketworld.infra.jpa.entity.id.ReservationStatisticsId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

internal interface ReservationStatisticsJpaRepository : JpaRepository<ReservationStatisticsEntity, ReservationStatisticsId> {
    @Query(
        value = """
            WITH counts AS (
                SELECT rs.performance_id, SUM(rs.count) AS count
                FROM reservation_statistics rs
                WHERE rs.standard_time > :startTime
                  AND rs.standard_time <= :standardTime
                GROUP BY rs.performance_id
                ORDER BY count DESC
                LIMIT :size
            )
            SELECT 
                BIN_TO_UUID(counts.performance_id) AS performanceId, 
                CAST((counts.count * 100.0 / total.total_count) AS DECIMAL(10,2)) AS reservationRate
            FROM counts,
                 (
                     SELECT SUM(rs2.count) AS total_count
                     FROM reservation_statistics rs2
                     WHERE rs2.standard_time > :startTime
                       AND rs2.standard_time <= :standardTime
                 ) total
        """,
        nativeQuery = true,
    )
    fun findPopularReservationStatistics(
        @Param("startTime") startTime: LocalDateTime,
        @Param("standardTime") standardTime: LocalDateTime,
        @Param("size") size: Int,
    ): List<PopularReservationStatistics>
}
