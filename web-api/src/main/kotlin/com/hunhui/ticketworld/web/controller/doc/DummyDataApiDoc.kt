package com.hunhui.ticketworld.web.controller.doc

import com.hunhui.ticketworld.application.dto.request.KopisPerformanceCreateRequest
import com.hunhui.ticketworld.application.dto.response.DummyDateCreateResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "DummyData", description = "더미 데이터 관련 API")
interface DummyDataApiDoc {
    @Operation(summary = "더미 데이터 생성 API")
    fun createDummyData(
        @RequestBody request: KopisPerformanceCreateRequest,
    ): ResponseEntity<DummyDateCreateResponse>
}
