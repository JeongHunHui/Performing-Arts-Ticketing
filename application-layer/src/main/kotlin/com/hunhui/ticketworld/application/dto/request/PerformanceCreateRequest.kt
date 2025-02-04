package com.hunhui.ticketworld.application.dto.request

import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PerformancePrice
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.seat.Seat
import com.hunhui.ticketworld.domain.seat.SeatArea
import java.time.LocalDateTime
import java.util.UUID

data class PerformanceCreateRequest(
    val title: String,
    val genre: PerformanceGenre,
    val imageUrl: String,
    val location: String,
    val description: String,
    val maxReservationCount: Int,
    val performancePrices: List<PerformancePriceRequest>,
    val rounds: List<PerformanceRoundRequest>,
    val seatAreas: List<SeatAreaRequest>,
) {
    fun toDomain(): Pair<Performance, List<SeatArea>> {
        val performance = getPerformance()
        val seatAreas = getSeatAreas(performance.id, performance.performancePrices)
        return performance to seatAreas
    }

    private fun getPerformance(): Performance =
        Performance.create(
            title = title,
            genre = genre,
            imageUrl = imageUrl,
            location = location,
            description = description,
            maxReservationCount = maxReservationCount,
            performancePrices =
                performancePrices.map {
                    PerformancePrice.create(
                        priceName = it.priceName,
                        price = it.price,
                    )
                },
            rounds =
                rounds.map {
                    PerformanceRound.create(
                        it.roundStartTime,
                        it.reservationStartTime,
                        it.reservationEndTime,
                    )
                },
        )

    private fun getSeatAreas(
        performanceId: UUID,
        performancePrices: List<PerformancePrice>,
    ): List<SeatArea> =
        seatAreas.map {
            SeatArea(
                id = UUID.randomUUID(),
                performanceId = performanceId,
                floorName = it.floorName,
                areaName = it.areaName,
                width = it.width,
                height = it.height,
                seats =
                    it.seats.map { seat ->
                        Seat(
                            id = UUID.randomUUID(),
                            performancePriceId = performancePrices[seat.priceIndex].id,
                            seatName = seat.seatName,
                            x = seat.x,
                            y = seat.y,
                        )
                    },
            )
        }

    data class PerformancePriceRequest(
        val priceName: String,
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
        val seats: List<SeatRequest>,
    )

    data class SeatRequest(
        val priceIndex: Int,
        val seatName: String,
        val x: Int,
        val y: Int,
    )
}
