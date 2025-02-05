package com.hunhui.ticketworld.domain.payment

import com.hunhui.ticketworld.common.vo.Money
import java.util.UUID

class PaymentItem(
    val id: UUID,
    val seatGradeName: String,
    val reservationCount: Int,
    val discountName: String,
    val originalPrice: Money,
    val discountedPrice: Money,
)
