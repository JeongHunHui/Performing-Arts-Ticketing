package com.hunhui.ticketworld.domain.performance.exception

import com.hunhui.ticketworld.common.error.ErrorCode

enum class KopisErrorCode(
    override val code: String,
    override val message: String,
) : ErrorCode {
    REQUEST_FAILED("KO001", "KOPIS API 요청에 실패했습니다."),
}
