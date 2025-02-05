package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.PaymentService
import com.hunhui.ticketworld.application.dto.request.PaymentCompleteRequest
import com.hunhui.ticketworld.application.dto.request.PaymentStartRequest
import com.hunhui.ticketworld.application.dto.response.PaymentStartResponse
import com.hunhui.ticketworld.web.controller.doc.PaymentApiDoc
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payment")
class PaymentController(
    private val paymentService: PaymentService,
) : PaymentApiDoc {
    @PatchMapping("/start")
    override fun startPayment(
        @RequestBody request: PaymentStartRequest,
    ): ResponseEntity<PaymentStartResponse> = ResponseEntity.ok(paymentService.startPayment(request))

    @PatchMapping("/confirm")
    override fun confirmPayment(
        @RequestBody request: PaymentCompleteRequest,
    ): ResponseEntity<Unit> = ResponseEntity.ok(paymentService.completePayment(request))
}
