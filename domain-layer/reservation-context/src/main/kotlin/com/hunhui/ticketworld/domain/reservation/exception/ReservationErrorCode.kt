package com.hunhui.ticketworld.domain.reservation.exception

import com.hunhui.ticketworld.common.error.ErrorCode

enum class ReservationErrorCode(
    override val code: String,
    override val message: String,
) : ErrorCode {
    RESERVATION_COUNT_EXCEED("RE001", "예매 가능 건수를 초과했습니다."),
    TICKET_IS_EMPTY("RE002", "예매에는 적어도 한 장의 티켓이 필요합니다."),
    ROUND_ID_NOT_UNIFIED("RE003", "예매는 하나의 회차에서만 가능합니다."),
    CANNOT_TEMP_RESERVE("RE004", "임시 예매할 수 없습니다."),
    CANNOT_CONFIRM_RESERVE("RE005", "예매를 확정할 수 없습니다."),
    NOT_FOUND("RE006", "예매를 찾을 수 없습니다."),
}
