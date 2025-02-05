package com.hunhui.ticketworld.domain.seatarea

import java.util.UUID

object SeatAreaFixtureFactory {
    fun createValidSeatPosition(
        id: UUID = UUID.randomUUID(),
        seatGradeId: UUID = UUID.randomUUID(),
        seatPositionName: String = "A1",
        x: Int = 0,
        y: Int = 1,
    ): SeatPosition =
        SeatPosition(
            id = id,
            seatGradeId = seatGradeId,
            number = seatPositionName,
            x = x,
            y = y,
        )

    fun createValidSeatArea(
        id: UUID = UUID.randomUUID(),
        performanceId: UUID = UUID.randomUUID(),
        floorName: String = "1층",
        areaName: String = "A구역",
        width: Int = 10,
        height: Int = 10,
        seatPositions: List<SeatPosition> = listOf(createValidSeatPosition(x = 1, y = 1), createValidSeatPosition(x = 2, y = 2)),
    ): SeatArea =
        SeatArea(
            id = id,
            performanceId = performanceId,
            floorName = floorName,
            areaName = areaName,
            width = width,
            height = height,
            positions = seatPositions,
        )
}
