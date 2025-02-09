package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.DummyPerformanceService
import com.hunhui.ticketworld.application.DummyReservationService
import com.hunhui.ticketworld.application.dto.request.DummyReservationCreateRequest
import com.hunhui.ticketworld.application.dto.request.KopisPerformanceCreateRequest
import com.hunhui.ticketworld.application.dto.response.DummyPerformanceCreateResponse
import com.hunhui.ticketworld.application.dto.response.DummyReservationCreateResponse
import com.hunhui.ticketworld.web.controller.doc.DummyDataApiDoc
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dummy-data")
class DummyDataController(
    private val dummyPerformanceService: DummyPerformanceService,
    private val dummyReservationService: DummyReservationService,
) : DummyDataApiDoc {
    @PostMapping("/performances")
    override suspend fun createDummyPerformances(request: KopisPerformanceCreateRequest): ResponseEntity<DummyPerformanceCreateResponse> =
        ResponseEntity.ok(dummyPerformanceService.createDummyPerformancesByKopis(request))

    @PostMapping("/reservations")
    override suspend fun createDummyReservations(request: DummyReservationCreateRequest): ResponseEntity<DummyReservationCreateResponse> =
        ResponseEntity.ok(dummyReservationService.createDummyReservations(request))
}
