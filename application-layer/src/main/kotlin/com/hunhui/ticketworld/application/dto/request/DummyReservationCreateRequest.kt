package com.hunhui.ticketworld.application.dto.request

import java.time.LocalDate

data class DummyReservationCreateRequest(
    val page: Int,
    val size: Int,
    val reservationRatio: Double,
    val minTicketsPerReservation: Int,
    val maxTicketsPerReservation: Int,
    val isNoReservation: Boolean,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)
