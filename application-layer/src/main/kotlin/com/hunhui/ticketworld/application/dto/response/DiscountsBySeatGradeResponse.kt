package com.hunhui.ticketworld.application.dto.response

import com.hunhui.ticketworld.domain.seatgrade.DiscountApplyCountType
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import java.math.BigDecimal
import java.util.UUID

data class DiscountsBySeatGradeResponse(
    val seatGrades: List<SeatGradeResponse>,
) {
    companion object {
        fun from(seatGrades: List<SeatGrade>) =
            DiscountsBySeatGradeResponse(
                seatGrades.map { seatGrade ->
                    SeatGradeResponse(
                        id = seatGrade.id,
                        discounts =
                            seatGrade.applicableDiscounts.map { discount ->
                                DiscountResponse(
                                    id = discount.id,
                                    name = discount.name,
                                    rate = discount.discountRate.rate,
                                    applyCountType = discount.applyCount.type,
                                    applyCountAmount = discount.applyCount.amount,
                                )
                            },
                    )
                },
            )
    }

    data class SeatGradeResponse(
        val id: UUID,
        val discounts: List<DiscountResponse>,
    )

    data class DiscountResponse(
        val id: UUID,
        val name: String,
        val rate: BigDecimal,
        val applyCountType: DiscountApplyCountType,
        val applyCountAmount: Int?,
    )
}
