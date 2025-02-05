package com.hunhui.ticketworld.web.controller.doc

import com.hunhui.ticketworld.application.dto.request.PaymentCompleteRequest
import com.hunhui.ticketworld.application.dto.request.PaymentStartRequest
import com.hunhui.ticketworld.application.dto.response.PaymentStartResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Payment", description = "결제 관련 API")
interface PaymentApiDoc {
    @Operation(summary = "결제 시작 API")
    fun startPayment(
        @RequestBody request: PaymentStartRequest,
    ): ResponseEntity<PaymentStartResponse>

    @Operation(summary = "결제 승인 API")
    fun confirmPayment(
        @RequestBody request: PaymentCompleteRequest,
    ): ResponseEntity<Unit>
}
