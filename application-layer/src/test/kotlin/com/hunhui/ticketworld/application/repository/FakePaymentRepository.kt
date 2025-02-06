package com.hunhui.ticketworld.application.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.payment.exception.PaymentErrorCode
import java.util.UUID

class FakePaymentRepository : PaymentRepository {
    private val payments = mutableMapOf<UUID, Payment>()

    override fun save(payment: Payment) {
        payments[payment.id] = payment
    }

    override fun getById(id: UUID): Payment = payments[id] ?: throw BusinessException(PaymentErrorCode.NOT_FOUND)
}
