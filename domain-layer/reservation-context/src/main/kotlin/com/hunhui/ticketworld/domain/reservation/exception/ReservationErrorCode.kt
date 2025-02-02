package com.hunhui.ticketworld.domain.reservation.exception

import com.hunhui.ticketworld.common.error.ErrorCode

enum class ReservationErrorCode(
    override val code: String,
    override val message: String,
) : ErrorCode {
    CANNOT_RESERVE("RE001", "예매할 수 없는 좌석입니다."),
    RESERVATION_COUNT_EXCEED("RE002", "예매 가능 건수를 초과했습니다."),
    INVALID_RESERVE_REQUEST("RE003", "예매 요청이 올바르지 않습니다."),
    INVALID_RESERVATIONS("RE004", "예매 목록이 옳바르지 않습니다."),
    CANNOT_TEMP_RESERVE("RE005", "임시 예매할 수 없습니다."),
}
