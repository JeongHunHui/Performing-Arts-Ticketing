package com.hunhui.ticketworld.application.dto.response

import com.hunhui.ticketworld.domain.reservation.Ticket
import java.util.UUID

data class TicketListResponse(
    val tickets: List<TicketResponse>,
) {
    companion object {
        fun from(ticketList: List<Ticket>): TicketListResponse =
            TicketListResponse(
                tickets =
                    ticketList.map {
                        TicketResponse(
                            id = it.id,
                            seatPositionId = it.seatPositionId,
                            canReserve = it.canTempReserve,
                        )
                    },
            )
    }

    data class TicketResponse(
        val id: UUID,
        val seatPositionId: UUID,
        val canReserve: Boolean,
    )
}
