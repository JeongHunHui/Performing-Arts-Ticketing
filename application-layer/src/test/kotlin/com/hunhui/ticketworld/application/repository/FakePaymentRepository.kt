package com.hunhui.ticketworld.application.repository

import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import java.util.UUID

class FakePaymentRepository : PaymentRepository {
    private val payments = mutableListOf<Payment>()

    override fun save(payment: Payment) {
        payments.add(payment)
    }

    override fun getById(id: UUID): Payment = payments.first { it.id == id }
}
