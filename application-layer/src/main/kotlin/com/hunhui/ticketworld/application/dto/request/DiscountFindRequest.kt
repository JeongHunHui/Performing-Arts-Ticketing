package com.hunhui.ticketworld.application.dto.request

import java.util.UUID

data class DiscountFindRequest(
    val seatGradeIds: List<UUID>,
)
