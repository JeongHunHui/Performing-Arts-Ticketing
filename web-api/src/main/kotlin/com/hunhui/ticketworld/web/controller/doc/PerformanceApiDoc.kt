package com.hunhui.ticketworld.web.controller.doc

import com.hunhui.ticketworld.application.dto.request.PerformanceCreateRequest
import com.hunhui.ticketworld.application.dto.response.PerformanceCreateResponse
import com.hunhui.ticketworld.application.dto.response.PerformanceResponse
import com.hunhui.ticketworld.application.dto.response.PerformanceSummaryListResponse
import com.hunhui.ticketworld.application.dto.response.SeatAreasResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@Tag(name = "Performance", description = "공연 관련 API")
interface PerformanceApiDoc {
    @Operation(summary = "공연 정보 API")
    fun getPerformance(
        @PathVariable("performanceId") performanceId: UUID,
    ): ResponseEntity<PerformanceResponse>

    @Operation(summary = "공연 정보 목록 조회 API")
    fun getPerformanceSummaryList(
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "0") page: Int,
    ): ResponseEntity<PerformanceSummaryListResponse>

    @Operation(summary = "공연 생성 API")
    fun createPerformance(
        @RequestBody request: PerformanceCreateRequest,
    ): ResponseEntity<PerformanceCreateResponse>

    @Operation(summary = "공연 좌석 영역 목록 조회 API")
    fun getSeatAreas(
        @PathVariable("performanceId") performanceId: UUID,
    ): ResponseEntity<SeatAreasResponse>
}
