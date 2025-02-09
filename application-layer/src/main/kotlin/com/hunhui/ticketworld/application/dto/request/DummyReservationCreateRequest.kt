package com.hunhui.ticketworld.application.dto.request

data class DummyReservationCreateRequest(
    val page: Int,
    val size: Int,
    val reservationRatio: Double,
    val minTicketsPerReservation: Int,
    val maxTicketsPerReservation: Int,
)
