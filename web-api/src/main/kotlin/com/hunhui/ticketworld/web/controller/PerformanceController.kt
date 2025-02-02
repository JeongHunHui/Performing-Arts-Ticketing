package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.PerformanceService
import com.hunhui.ticketworld.application.dto.request.PerformanceCreateRequest
import com.hunhui.ticketworld.application.dto.response.PerformanceCreateResponse
import com.hunhui.ticketworld.application.dto.response.PerformanceResponse
import com.hunhui.ticketworld.application.dto.response.PerformanceSummaryListResponse
import com.hunhui.ticketworld.application.dto.response.SeatAreasResponse
import com.hunhui.ticketworld.web.controller.doc.PerformanceApiDoc
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/performance")
class PerformanceController(
    private val performanceService: PerformanceService,
) : PerformanceApiDoc {
    @GetMapping("/{performanceId}")
    override fun getPerformance(
        @PathVariable("performanceId") performanceId: UUID,
    ): ResponseEntity<PerformanceResponse> = ResponseEntity.ok(performanceService.getPerformance(performanceId))

    @GetMapping
    override fun getPerformanceSummaryList(
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "0") page: Int,
    ): ResponseEntity<PerformanceSummaryListResponse> = ResponseEntity.ok(performanceService.getPerformances(page, size))

    @PostMapping
    override fun createPerformance(
        @RequestBody performanceCreateRequest: PerformanceCreateRequest,
    ): ResponseEntity<PerformanceCreateResponse> = ResponseEntity.ok(performanceService.createPerformance(performanceCreateRequest))

    @GetMapping("/{performanceId}/seat-areas")
    override fun getSeatAreas(
        @PathVariable("performanceId") performanceId: UUID,
    ): ResponseEntity<SeatAreasResponse> = ResponseEntity.ok(performanceService.getSeatAreas(performanceId))
}
