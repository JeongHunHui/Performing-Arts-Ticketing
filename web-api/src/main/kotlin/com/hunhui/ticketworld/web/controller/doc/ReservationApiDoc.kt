package com.hunhui.ticketworld.web.controller.doc

import com.hunhui.ticketworld.application.dto.request.ReserveRequest
import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.ReservationListResponse
import com.hunhui.ticketworld.application.dto.response.ReserveResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@Tag(name = "Reservation", description = "예매 관련 API")
interface ReservationApiDoc {
    @Operation(summary = "예매 목록 API")
    fun findAllReservations(
        @RequestParam roundId: UUID,
        @RequestParam areaId: UUID,
    ): ResponseEntity<ReservationListResponse>

    @Operation(summary = "임시 예매 API")
    fun tempReserve(
        @RequestBody tempReserveRequest: TempReserveRequest,
    ): ResponseEntity<Unit>

    @Operation(summary = "예매 API")
    fun reserve(
        @RequestBody reserveRequest: ReserveRequest,
    ): ResponseEntity<ReserveResponse>
}
