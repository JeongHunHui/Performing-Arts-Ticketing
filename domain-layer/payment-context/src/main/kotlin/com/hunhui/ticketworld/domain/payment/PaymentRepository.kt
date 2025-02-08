package com.hunhui.ticketworld.domain.payment

import java.util.UUID

interface PaymentRepository {
    fun getById(id: UUID): Payment

    fun save(payment: Payment)

    fun saveAll(payments: List<Payment>)
}
