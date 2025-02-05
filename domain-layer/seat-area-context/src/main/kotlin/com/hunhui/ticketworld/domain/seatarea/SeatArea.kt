package com.hunhui.ticketworld.domain.seatarea

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.seatarea.exception.SeatAreaErrorCode.SEAT_IS_EMPTY
import com.hunhui.ticketworld.domain.seatarea.exception.SeatAreaErrorCode.SEAT_NOT_CONTAINED
import com.hunhui.ticketworld.domain.seatarea.exception.SeatAreaErrorCode.WIDTH_HEIGHT_IS_NOT_POSITIVE
import java.util.UUID

class SeatArea(
    val id: UUID,
    val performanceId: UUID,
    val floorName: String,
    val areaName: String,
    val width: Int,
    val height: Int,
    val positions: List<SeatPosition>,
) {
    init {
        if (width <= 0 || height <= 0) throw BusinessException(WIDTH_HEIGHT_IS_NOT_POSITIVE)
        if (positions.isEmpty()) throw BusinessException(SEAT_IS_EMPTY)
        if (allSeatsContained.not()) throw BusinessException(SEAT_NOT_CONTAINED)
    }

    /**
     * 좌석이 영역의 너비와 높이에 포함되는지 확인
     * @return 좌석이 영역에 포함되면 true, 아니면 false
     */
    private fun SeatPosition.isContained(): Boolean = this.x < width && this.y < height

    private val allSeatsContained: Boolean
        get() = positions.all { it.isContained() }
}
