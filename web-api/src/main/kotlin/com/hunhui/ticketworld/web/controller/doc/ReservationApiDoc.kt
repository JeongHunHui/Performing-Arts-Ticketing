package com.hunhui.ticketworld.web.controller.doc

import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.TempReserveResponse
import com.hunhui.ticketworld.application.dto.response.TicketListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@Tag(name = "Reservation", description = "예매 관련 API")
interface ReservationApiDoc {
    @Operation(summary = "티켓 예매 여부 목록 API")
    fun findAllTickets(
        @RequestParam roundId: UUID,
        @RequestParam areaId: UUID,
    ): ResponseEntity<TicketListResponse>

    @Operation(summary = "티켓 임시 예매 API")
    fun tempReserve(
        @RequestBody request: TempReserveRequest,
    ): ResponseEntity<TempReserveResponse>
}
