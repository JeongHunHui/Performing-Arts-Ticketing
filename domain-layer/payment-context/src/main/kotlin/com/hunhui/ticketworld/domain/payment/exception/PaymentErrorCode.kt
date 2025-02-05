package com.hunhui.ticketworld.domain.payment.exception

import com.hunhui.ticketworld.common.error.ErrorCode

enum class PaymentErrorCode(
    override val code: String,
    override val message: String,
) : ErrorCode {
    NOT_FOUND("PA001", "결제 정보를 찾을 수 없습니다."),
    CANNOT_COMPLETE("PA002", "결제를 완료할 수 없습니다."),
    INVALID_START_PAYMENT_REQUEST("PA003", "결제 시작 요청이 옳바르지 않습니다."),
}
