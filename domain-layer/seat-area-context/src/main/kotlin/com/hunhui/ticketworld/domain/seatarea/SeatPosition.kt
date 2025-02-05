package com.hunhui.ticketworld.domain.seatarea

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.seatarea.exception.SeatAreaErrorCode.POSITION_IS_NEGATIVE
import java.util.UUID

class SeatPosition(
    val id: UUID,
    val seatGradeId: UUID,
    val name: String,
    val x: Int,
    val y: Int,
) {
    init {
        if (x < 0 || y < 0) throw BusinessException(POSITION_IS_NEGATIVE)
    }
}
