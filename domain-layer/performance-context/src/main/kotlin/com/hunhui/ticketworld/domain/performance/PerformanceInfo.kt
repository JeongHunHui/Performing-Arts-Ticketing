package com.hunhui.ticketworld.domain.performance

data class PerformanceInfo(
    val kopisId: String? = null,
    val title: String,
    val genre: PerformanceGenre,
    val posterUrl: String? = null,
    val kopisFacilityId: String? = null,
    val location: String,
    val locationAddress: String? = null,
    val cast: String? = null,
    val crew: String? = null,
    val runtime: String? = null,
    val ageLimit: String? = null,
    val seatScale: Int? = null,
    val descriptionImageUrls: List<String> = emptyList(),
)
