package com.hunhui.ticketworld.application.dto.request

import com.hunhui.ticketworld.domain.seatgrade.Discount
import com.hunhui.ticketworld.domain.seatgrade.DiscountApplyCountType
import com.hunhui.ticketworld.domain.seatgrade.DiscountCondition
import java.math.BigDecimal

data class DiscountCreateRequest(
    val discountName: String,
    val discountConditions: List<DiscountCondition>,
    val applyCountType: DiscountApplyCountType,
    val applyCountAmount: Int?,
    val discountRate: BigDecimal,
) {
    fun toDomain(): Discount =
        Discount.create(
            name = discountName,
            conditions = discountConditions,
            applyCountType = applyCountType,
            applyCountAmount = applyCountAmount,
            rate = discountRate,
        )
}
