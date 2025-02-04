package com.hunhui.ticketworld.domain.payment.exception

import com.hunhui.ticketworld.common.error.ErrorCode

enum class PaymentErrorCode(
    override val code: String,
    override val message: String,
) : ErrorCode {
    NOT_FOUND("PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
}
