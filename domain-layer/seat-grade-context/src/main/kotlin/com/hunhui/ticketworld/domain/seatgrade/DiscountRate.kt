package com.hunhui.ticketworld.domain.seatgrade

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.seatgrade.exception.SeatGradeErrorCode.INVALID_DISCOUNT_RATE
import java.math.BigDecimal

data class DiscountRate(
    val rate: BigDecimal,
) {
    init {
        if (rate !in BigDecimal(0)..BigDecimal(1)) throw BusinessException(INVALID_DISCOUNT_RATE)
    }

    internal fun apply(price: Money): Money = Money(BigDecimal(price.amount).multiply(BigDecimal(1) - rate).toLong())
}
