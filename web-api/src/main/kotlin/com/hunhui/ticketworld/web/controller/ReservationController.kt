package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.ReservationService
import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.TempReserveResponse
import com.hunhui.ticketworld.application.dto.response.TicketListResponse
import com.hunhui.ticketworld.web.controller.doc.ReservationApiDoc
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/reservation")
class ReservationController(
    private val reservationService: ReservationService,
) : ReservationApiDoc {
    @GetMapping
    override fun findAllTickets(
        @RequestParam roundId: UUID,
        @RequestParam areaId: UUID,
    ): ResponseEntity<TicketListResponse> = ResponseEntity.ok(reservationService.findAll(roundId, areaId))

    @PatchMapping("/temp")
    override fun tempReservation(
        @RequestBody tempReserveRequest: TempReserveRequest,
    ): ResponseEntity<TempReserveResponse> = ResponseEntity.ok(reservationService.tempReserve(tempReserveRequest))
}
