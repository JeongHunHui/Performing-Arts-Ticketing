package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.DummyDataService
import com.hunhui.ticketworld.application.dto.request.KopisPerformanceCreateRequest
import com.hunhui.ticketworld.web.controller.doc.DummyDataApiDoc
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dummy-data")
class DummyDataController(
    private val dummyDataService: DummyDataService,
) : DummyDataApiDoc {
    @PostMapping
    override fun createDummyData(request: KopisPerformanceCreateRequest): ResponseEntity<Unit> =
        ResponseEntity.ok(dummyDataService.createDummyData(request))
}
