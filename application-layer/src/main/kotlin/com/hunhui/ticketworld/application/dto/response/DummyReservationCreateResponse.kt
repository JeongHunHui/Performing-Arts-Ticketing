package com.hunhui.ticketworld.application.dto.response

data class DummyReservationCreateResponse(
    val skippedCount: Int,
    val successCount: Int,
    val failedCount: Int,
    val processingTimeMillis: Long,
    val totalRoundsCount: Int,
    val totalTicketsCount: Int,
    val totalReservationsCount: Int,
)
