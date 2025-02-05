package com.hunhui.ticketworld.web.controller.doc

import com.hunhui.ticketworld.application.dto.request.DiscountCreateRequest
import com.hunhui.ticketworld.application.dto.request.DiscountFindRequest
import com.hunhui.ticketworld.application.dto.response.DiscountsBySeatGradeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Tag(name = "SeatGrade", description = "좌석 등급 관련 API")
interface SeatGradeApiDoc {
    @Operation(summary = "할인 생성 API")
    fun addDiscountInSeatGrade(
        @PathVariable seatGradeId: UUID,
        @RequestBody request: DiscountCreateRequest,
    ): ResponseEntity<Unit>

    @Operation(summary = "좌석 등급 별 적용 가능 할인 목록 조회 API")
    fun findApplicableDiscountsBySeatGrade(
        @RequestBody request: DiscountFindRequest,
    ): ResponseEntity<DiscountsBySeatGradeResponse>
}
