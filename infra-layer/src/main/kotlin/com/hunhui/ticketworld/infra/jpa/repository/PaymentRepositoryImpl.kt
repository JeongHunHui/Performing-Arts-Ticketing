package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentItem
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.payment.exception.PaymentErrorCode.NOT_FOUND
import com.hunhui.ticketworld.infra.jpa.entity.PaymentEntity
import com.hunhui.ticketworld.infra.jpa.entity.PaymentItemEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class PaymentRepositoryImpl(
    private val paymentJpaRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun getById(id: UUID): Payment = paymentJpaRepository.findByIdOrNull(id)?.domain ?: throw BusinessException(NOT_FOUND)

    override fun save(payment: Payment) {
        paymentJpaRepository.save(payment.entity)
    }

    private val PaymentEntity.domain: Payment
        get() =
            Payment(
                id = id,
                userId = userId,
                status = status,
                method = method,
                items =
                    items.map {
                        PaymentItem(
                            id = it.id,
                            seatGradeId = it.seatGradeId,
                            reservationCount = it.reservationCount,
                            discountId = it.discountId,
                            paymentAmount = Money(it.paymentAmount),
                        )
                    },
            )

    private val Payment.entity: PaymentEntity
        get() =
            PaymentEntity(
                id = id,
                userId = userId,
                status = status,
                method = method,
                items =
                    items.map {
                        PaymentItemEntity(
                            id = it.id,
                            paymentId = id,
                            seatGradeId = it.seatGradeId,
                            reservationCount = it.reservationCount,
                            discountId = it.discountId,
                            paymentAmount = it.paymentAmount.amount,
                        )
                    },
            )
}
