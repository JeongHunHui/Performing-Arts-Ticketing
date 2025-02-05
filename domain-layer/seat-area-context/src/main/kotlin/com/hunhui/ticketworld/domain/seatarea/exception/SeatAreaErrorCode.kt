package com.hunhui.ticketworld.domain.seatarea.exception

import com.hunhui.ticketworld.common.error.ErrorCode

enum class SeatAreaErrorCode(
    override val code: String,
    override val message: String,
) : ErrorCode {
    POSITION_IS_NEGATIVE("SA001", "좌석의 좌표는 0 이상이어야 합니다."),
    WIDTH_HEIGHT_IS_NOT_POSITIVE("SA002", "공간의 너비나 높이는 1 이상이어야 합니다."),
    SEAT_IS_EMPTY("SA003", "좌석은 비어있을 수 없습니다."),
    SEAT_NOT_CONTAINED("SA004", "좌석이 영역 내에 포함되어 있어야 합니다."),
}
