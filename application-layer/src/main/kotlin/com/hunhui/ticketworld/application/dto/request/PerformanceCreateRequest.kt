package com.hunhui.ticketworld.application.dto.request

import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.seatarea.SeatArea
import com.hunhui.ticketworld.domain.seatarea.SeatPosition
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import java.time.LocalDateTime
import java.util.UUID

data class PerformanceCreateRequest(
    val title: String,
    val genre: PerformanceGenre,
    val imageUrl: String,
    val location: String,
    val description: String,
    val maxReservationCount: Int,
    val seatGrades: List<SeatGradeRequest>,
    val rounds: List<PerformanceRoundRequest>,
    val seatAreas: List<SeatAreaRequest>,
) {
    fun toDomain(): Triple<Performance, List<SeatGrade>, List<SeatArea>> {
        val performance = getPerformance()
        val seatGrades = getSeatGrades(performance.id)
        val seatAreas = getSeatAreas(performance.id, seatGrades)
        return Triple(performance, seatGrades, seatAreas)
    }

    private fun getPerformance(): Performance =
        Performance.create(
            title = title,
            genre = genre,
            imageUrl = imageUrl,
            location = location,
            description = description,
            maxReservationCount = maxReservationCount,
            rounds =
                rounds.map {
                    PerformanceRound.create(
                        it.roundStartTime,
                        it.reservationStartTime,
                        it.reservationEndTime,
                    )
                },
        )

    private fun getSeatGrades(performanceId: UUID): List<SeatGrade> =
        seatGrades.map {
            SeatGrade.create(
                performanceId = performanceId,
                name = it.name,
                price = it.price,
            )
        }

    private fun getSeatAreas(
        performanceId: UUID,
        seatGrades: List<SeatGrade>,
    ): List<SeatArea> =
        seatAreas.map {
            SeatArea(
                id = UUID.randomUUID(),
                performanceId = performanceId,
                floorName = it.floorName,
                areaName = it.areaName,
                width = it.width,
                height = it.height,
                positions =
                    it.positions.map { seat ->
                        SeatPosition(
                            id = UUID.randomUUID(),
                            seatGradeId = seatGrades[seat.seatGradeIndex].id,
                            name = seat.name,
                            x = seat.x,
                            y = seat.y,
                        )
                    },
            )
        }

    data class SeatGradeRequest(
        val name: String,
        val price: Long,
    )

    data class PerformanceRoundRequest(
        val roundStartTime: LocalDateTime,
        val reservationStartTime: LocalDateTime,
        val reservationEndTime: LocalDateTime,
    )

    data class SeatAreaRequest(
        val floorName: String,
        val areaName: String,
        val width: Int,
        val height: Int,
        val positions: List<SeatPositionRequest>,
    )

    data class SeatPositionRequest(
        val seatGradeIndex: Int,
        val name: String,
        val x: Int,
        val y: Int,
    )
}
