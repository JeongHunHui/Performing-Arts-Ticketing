package com.hunhui.ticketworld.application.dto.response

import com.hunhui.ticketworld.domain.seatarea.SeatArea
import java.util.UUID

data class SeatAreasResponse(
    val seatAreas: List<SeatAreaResponse>,
) {
    companion object {
        fun from(seatAreas: List<SeatArea>): SeatAreasResponse =
            SeatAreasResponse(
                seatAreas.map { seatArea ->
                    SeatAreaResponse(
                        id = seatArea.id,
                        floorName = seatArea.floorName,
                        areaName = seatArea.areaName,
                        width = seatArea.width,
                        height = seatArea.height,
                        positions =
                            seatArea.positions.map { seat ->
                                SeatPositionResponse(
                                    id = seat.id,
                                    seatGradeId = seat.seatGradeId,
                                    name = seat.number,
                                    x = seat.x,
                                    y = seat.y,
                                )
                            },
                    )
                },
            )
    }

    data class SeatAreaResponse(
        val id: UUID,
        val floorName: String,
        val areaName: String,
        val width: Int,
        val height: Int,
        val positions: List<SeatPositionResponse>,
    )

    data class SeatPositionResponse(
        val id: UUID,
        val seatGradeId: UUID,
        val name: String,
        val x: Int,
        val y: Int,
    )
}
