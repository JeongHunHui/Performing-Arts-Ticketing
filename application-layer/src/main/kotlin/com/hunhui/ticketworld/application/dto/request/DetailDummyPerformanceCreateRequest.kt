package com.hunhui.ticketworld.application.dto.request

import java.time.LocalDateTime

data class DetailDummyPerformanceCreateRequest(
    val kopisId: String,
    val seatAreaSettings: List<SeatAreaSetting>,
    val maxReservationCount: Int,
    val schedules: List<LocalDateTime>?,
) {
    data class SeatAreaSetting(
        val floorName: String,
        val areaName: String,
        val seatGradeNames: List<String>,
        val seatCount: Int,
    )
}
