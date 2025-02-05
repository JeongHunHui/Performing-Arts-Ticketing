package com.hunhui.ticketworld.domain.seatgrade.exception

import com.hunhui.ticketworld.common.error.ErrorCode

enum class SeatGradeErrorCode(
    override val code: String,
    override val message: String,
) : ErrorCode {
    INVALID_DISCOUNT_RATE("SG001", "할인율은 0이상 1이하의 실수입니다."),
    CANNOT_DISCOUNT("SG002", "할인을 적용할 수 없습니다."),
    NOT_FOUND("SG003", "좌석 등급을 찾을 수 없습니다."),
    INVALID_DISCOUNT_APPLY_COUNT("SG004", "할인 적용 수량이 유효하지 않습니다."),
}
