package com.hunhui.ticketworld.application.dto.response

import com.hunhui.ticketworld.domain.reservation.Ticket
import java.util.UUID

data class ReservationListResponse(
    val reservations: List<ReservationResponse>,
) {
    companion object {
        fun from(ticketList: List<Ticket>): ReservationListResponse =
            ReservationListResponse(
                reservations =
                    ticketList.map {
                        ReservationResponse(
                            id = it.id,
                            seatId = it.seatId,
                            canReserve = it.canTempReserve,
                        )
                    },
            )
    }

    data class ReservationResponse(
        val id: UUID,
        val seatId: UUID,
        val canReserve: Boolean,
    )
}
