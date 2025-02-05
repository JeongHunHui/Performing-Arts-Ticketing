package com.hunhui.ticketworld.domain.seatgrade

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.seatgrade.exception.SeatGradeErrorCode.CANNOT_DISCOUNT
import com.hunhui.ticketworld.domain.seatgrade.exception.SeatGradeErrorCode.INVALID_DISCOUNT_APPLY_COUNT
import java.math.BigDecimal
import java.util.UUID

class Discount(
    val id: UUID,
    val name: String,
    val conditions: List<DiscountCondition>,
    val applyCount: DiscountApplyCount,
    val discountRate: DiscountRate,
) {
    companion object {
        fun create(
            name: String,
            conditions: List<DiscountCondition>,
            applyCountType: DiscountApplyCountType,
            applyCountAmount: Int?,
            rate: BigDecimal,
        ) = Discount(
            id = UUID.randomUUID(),
            name = name,
            conditions = conditions,
            applyCount = DiscountApplyCount.create(applyCountType, applyCountAmount),
            discountRate = DiscountRate(rate),
        )

        internal val DEFAULT =
            create(
                name = "일반",
                conditions = emptyList(),
                applyCountType = DiscountApplyCountType.INF,
                applyCountAmount = null,
                rate = BigDecimal.ZERO,
            )
    }

    internal fun canApply() = conditions.all { it.canApply() }

    internal fun apply(
        price: Money,
        count: Int,
    ): Money {
        if (!canApply()) throw BusinessException(CANNOT_DISCOUNT)
        if (!applyCount.canApply(count)) throw BusinessException(INVALID_DISCOUNT_APPLY_COUNT)
        return discountRate.apply(price * count)
    }
}
