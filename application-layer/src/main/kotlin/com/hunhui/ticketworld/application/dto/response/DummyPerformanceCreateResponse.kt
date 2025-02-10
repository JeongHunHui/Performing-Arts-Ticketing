package com.hunhui.ticketworld.application.dto.response

data class DummyPerformanceCreateResponse(
    val skippedCount: Int,
    val successCount: Int,
    val failedCount: Int,
    val processingTimeMillis: Long,
    val totalRoundsCount: Int,
)
