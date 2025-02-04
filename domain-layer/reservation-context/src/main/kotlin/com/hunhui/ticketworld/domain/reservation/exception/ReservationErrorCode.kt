package com.hunhui.ticketworld.domain.reservation.exception

import com.hunhui.ticketworld.common.error.ErrorCode

enum class ReservationErrorCode(
    override val code: String,
    override val message: String,
) : ErrorCode {
    CANNOT_RESERVE("RE001", "예매할 수 없는 좌석입니다."),
    RESERVATION_COUNT_EXCEED("RE002", "예매 가능 건수를 초과했습니다."),
    INVALID_RESERVE_REQUEST("RE003", "예매 요청이 옳바르지 않습니다."),
    INVALID_RESERVATION("RE004", "예매 목록이 옳바르지 않습니다."),
    CANNOT_TEMP_RESERVE("RE005", "임시 예매할 수 없습니다."),
    CANNOT_CONFIRM_RESERVE("RE006", "예매를 확정할 수 없습니다."),
    ROUND_NOT_AVAILABLE("RE007", "선택한 회차의 예매 일정이 지났습니다."),
    NOT_FOUND("RE008", "예매를 찾을 수 없습니다."),
    EXPIRED("RE009", "만료된 예매입니다."),
}
