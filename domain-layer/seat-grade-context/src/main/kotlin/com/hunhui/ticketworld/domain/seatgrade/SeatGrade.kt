package com.hunhui.ticketworld.domain.seatgrade

import com.hunhui.ticketworld.common.vo.Money
import java.util.UUID

class SeatGrade(
    val id: UUID,
    val performanceId: UUID,
    val name: String,
    val price: Money,
    val discounts: List<Discount>,
) {
    companion object {
        fun create(
            performanceId: UUID,
            name: String,
            price: Long,
        ) = SeatGrade(
            id = UUID.randomUUID(),
            performanceId = performanceId,
            name = name,
            price = Money(price),
            discounts = emptyList(),
        )
    }

    val applicableDiscounts: List<Discount>
        get() = discounts.filter { it.canApply() }

    fun addDiscount(discount: Discount): SeatGrade =
        SeatGrade(
            id = id,
            performanceId = performanceId,
            name = name,
            price = price,
            discounts = discounts + discount,
        )

    fun calculatePaymentAmount(
        discountId: UUID?,
        reservationCount: Int,
    ): Money {
        val discount = getDiscountById(discountId)
        return discount.apply(price, reservationCount)
    }

    private fun getDiscountById(discountId: UUID?): Discount {
        if (discountId == null) return Discount.DEFAULT
        return discounts.first { it.id == discountId }
    }
}
