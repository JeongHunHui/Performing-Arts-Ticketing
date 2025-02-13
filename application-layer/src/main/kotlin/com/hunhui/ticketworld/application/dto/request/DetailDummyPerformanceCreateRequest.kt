package com.hunhui.ticketworld.application.dto.request

data class DetailDummyPerformanceCreateRequest(
    val kopisId: String,
    val seatAreaSettings: List<SeatAreaSetting>,
) {
    data class SeatAreaSetting(
        val floorName: String,
        val areaName: String,
        val seatGradeNames: List<String>,
        val seatCount: Int,
    )
}
