package com.hunhui.ticketworld.application.dto.request

import java.util.UUID

data class TempReserveRequest(
    val performanceId: UUID,
    val roundId: UUID,
    val userId: UUID,
    val ticketIds: List<UUID>,
)
