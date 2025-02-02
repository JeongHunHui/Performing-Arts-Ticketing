package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.DiscountService
import com.hunhui.ticketworld.application.dto.request.DiscountCreateRequest
import com.hunhui.ticketworld.application.dto.request.DiscountFindRequest
import com.hunhui.ticketworld.application.dto.response.DiscountListResponse
import com.hunhui.ticketworld.web.controller.doc.DiscountApiDoc
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/discount")
class DiscountController(
    private val discountService: DiscountService,
) : DiscountApiDoc {
    @PostMapping("/create/{performanceId}")
    override fun createDiscount(
        @PathVariable performanceId: UUID,
        @RequestBody discountCreateRequest: DiscountCreateRequest,
    ): ResponseEntity<Unit> = ResponseEntity.ok(discountService.createDiscount(performanceId, discountCreateRequest))

    @PostMapping("/find/{performanceId}")
    override fun findAllDiscount(
        @PathVariable performanceId: UUID,
        @RequestBody discountFindRequest: DiscountFindRequest,
    ): ResponseEntity<DiscountListResponse> =
        ResponseEntity.ok(discountService.findAllApplicableDiscounts(performanceId, discountFindRequest))
}
