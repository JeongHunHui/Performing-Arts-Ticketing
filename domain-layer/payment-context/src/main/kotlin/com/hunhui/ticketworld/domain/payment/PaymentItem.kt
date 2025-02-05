package com.hunhui.ticketworld.domain.payment

import com.hunhui.ticketworld.common.vo.Money
import java.util.UUID

class PaymentItem(
    val id: UUID,
    val seatGradeId: UUID,
    val reservationCount: Int,
    val discountId: UUID?,
    val paymentAmount: Money,
)
