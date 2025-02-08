package com.hunhui.ticketworld.domain.kopis

data class KopisPerformanceFacility(
    val id: String,
    val name: String,
    val address: String,
    val places: List<Place>,
) {
    data class Place(
        val id: String,
        val name: String,
        val seatScale: Int?,
    )

    fun getSeatScaleByFullName(fullName: String): Int? = places.first { fullName == "$name (${it.name})" }.seatScale
}
