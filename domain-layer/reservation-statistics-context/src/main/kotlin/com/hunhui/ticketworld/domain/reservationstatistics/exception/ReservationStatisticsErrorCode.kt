package com.hunhui.ticketworld.domain.reservationstatistics.exception

import com.hunhui.ticketworld.common.error.ErrorCode

enum class ReservationStatisticsErrorCode(
    override val code: String,
    override val message: String,
) : ErrorCode {
    NOT_FOUND("RS001", "예매 통계를 찾을 수 없습니다."),
}
