package com.hunhui.ticketworld.web.controller

import com.hunhui.ticketworld.application.PaymentService
import com.hunhui.ticketworld.application.dto.request.CompletePaymentRequest
import com.hunhui.ticketworld.application.dto.request.StartPaymentRequest
import com.hunhui.ticketworld.application.dto.response.StartPaymentResponse
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
        @RequestBody startPaymentRequest: StartPaymentRequest,
    ): ResponseEntity<StartPaymentResponse> = ResponseEntity.ok(paymentService.startPayment(startPaymentRequest))

    @PatchMapping("/confirm")
    override fun confirmPayment(
        @RequestBody completePaymentRequest: CompletePaymentRequest,
    ): ResponseEntity<Unit> = ResponseEntity.ok(paymentService.completePayment(completePaymentRequest))
}
