package com.hunhui.ticketworld.web.controller.doc

import com.hunhui.ticketworld.application.dto.request.DummyReservationCreateRequest
import com.hunhui.ticketworld.application.dto.request.KopisPerformanceCreateRequest
import com.hunhui.ticketworld.application.dto.response.DummyPerformanceCreateResponse
import com.hunhui.ticketworld.application.dto.response.DummyReservationCreateResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "DummyData", description = "더미 데이터 관련 API")
interface DummyDataApiDoc {
    @Operation(summary = "Kopis 공연를 통한 공연 생성 API")
    suspend fun createDummyPerformances(
        @RequestBody request: KopisPerformanceCreateRequest,
    ): ResponseEntity<DummyPerformanceCreateResponse>

    @Operation(summary = "공연의 더미 예매 및 결제 생성 API")
    suspend fun createDummyReservations(
        @RequestBody request: DummyReservationCreateRequest,
    ): ResponseEntity<DummyReservationCreateResponse>
}
