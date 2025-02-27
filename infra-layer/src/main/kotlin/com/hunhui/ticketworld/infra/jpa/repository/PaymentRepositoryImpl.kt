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

    override fun saveAll(payments: List<Payment>) {
        paymentJpaRepository.saveAll(payments.map { it.entity })
    }

    override fun findAllPaymentByUserIdAndRoundId(
        userId: UUID,
        roundId: UUID,
    ): List<Payment> =
        paymentJpaRepository.findAllByUserIdAndPerformanceRoundId(userId, roundId).map {
            it.domain
        }

    override fun findAllPaymentByUserIdAndRoundIdWithPessimistic(
        userId: UUID,
        roundId: UUID,
    ): List<Payment> =
        paymentJpaRepository.findAllByUserIdAndPerformanceRoundIdWithPessimistic(userId, roundId).map {
            it.domain
        }

    private val PaymentEntity.domain: Payment
        get() =
            Payment(
                id = id,
                userId = userId,
                roundId = performanceRoundId,
                status = status,
                method = method,
                items =
                    items
                        .map {
                            PaymentItem(
                                id = it.id,
                                seatGradeName = it.seatGradeName,
                                reservationCount = it.reservationCount,
                                discountName = it.discountName,
                                originalPrice = Money(it.originalPrice),
                                discountedPrice = Money(it.discountedPrice),
                            )
                        }.toMutableList(),
            )

    private val Payment.entity: PaymentEntity
        get() =
            PaymentEntity(
                id = id,
                userId = userId,
                performanceRoundId = roundId,
                status = status,
                method = method,
                items =
                    items.map {
                        PaymentItemEntity(
                            id = it.id,
                            paymentId = id,
                            seatGradeName = it.seatGradeName,
                            reservationCount = it.reservationCount,
                            discountName = it.discountName,
                            originalPrice = it.originalPrice.amount,
                            discountedPrice = it.discountedPrice.amount,
                        )
                    },
            )
}
