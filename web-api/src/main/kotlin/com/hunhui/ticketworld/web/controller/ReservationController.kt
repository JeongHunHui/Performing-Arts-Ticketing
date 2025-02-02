package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.ReservationService
import com.hunhui.ticketworld.application.dto.request.ReserveRequest
import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.ReservationListResponse
import com.hunhui.ticketworld.application.dto.response.ReserveResponse
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
    override fun findAllReservations(
        @RequestParam roundId: UUID,
        @RequestParam areaId: UUID,
    ): ResponseEntity<ReservationListResponse> = ResponseEntity.ok(reservationService.findAll(roundId, areaId))

    @PatchMapping("/temp-reserve")
    override fun tempReserve(
        @RequestBody tempReserveRequest: TempReserveRequest,
    ): ResponseEntity<Unit> = ResponseEntity.ok(reservationService.tempReserve(tempReserveRequest))

    @PatchMapping("/reserve")
    override fun reserve(
        @RequestBody reserveRequest: ReserveRequest,
    ): ResponseEntity<ReserveResponse> = ResponseEntity.ok(reservationService.reserve(reserveRequest))
}
