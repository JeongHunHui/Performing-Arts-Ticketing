package com.hunhui.ticketworld.domain.kopis

data class KopisPerformanceFacility(
    val name: String,
    val id: String,
    val address: String,
    val places: List<Place>,
) {
    data class Place(
        val roomName: String,
        val seatScale: String,
    )
}
