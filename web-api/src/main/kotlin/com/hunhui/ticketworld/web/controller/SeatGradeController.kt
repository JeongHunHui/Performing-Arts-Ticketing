package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.SeatGradeService
import com.hunhui.ticketworld.application.dto.request.DiscountCreateRequest
import com.hunhui.ticketworld.application.dto.request.DiscountFindRequest
import com.hunhui.ticketworld.application.dto.response.DiscountsBySeatGradeResponse
import com.hunhui.ticketworld.web.controller.doc.SeatGradeApiDoc
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/seat-grade")
class SeatGradeController(
    private val seatGradeService: SeatGradeService,
) : SeatGradeApiDoc {
    @PostMapping("/{seatGradeId}/discount")
    override fun addDiscountInSeatGrade(
        @PathVariable seatGradeId: UUID,
        @RequestBody request: DiscountCreateRequest,
    ): ResponseEntity<Unit> = ResponseEntity.ok(seatGradeService.addDiscountInSeatGrade(seatGradeId, request))

    @PostMapping("/find-applicable-discounts")
    override fun findApplicableDiscountsBySeatGrade(
        @RequestBody request: DiscountFindRequest,
    ): ResponseEntity<DiscountsBySeatGradeResponse> = ResponseEntity.ok(seatGradeService.findApplicableDiscountsBySeatGrade(request))
}
