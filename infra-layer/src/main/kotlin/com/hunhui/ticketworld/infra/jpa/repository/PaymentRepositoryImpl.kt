package com.hunhui.ticketworld.infra.jpa.repository

import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentInfo
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.payment.exception.PaymentErrorCode
import com.hunhui.ticketworld.infra.jpa.entity.PaymentEntity
import com.hunhui.ticketworld.infra.jpa.entity.PaymentInfoEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal class PaymentRepositoryImpl(
    private val paymentJpaRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun getById(id: UUID): Payment =
        paymentJpaRepository.findByIdOrNull(id)?.domain ?: throw BusinessException(PaymentErrorCode.NOT_FOUND)

    override fun save(payment: Payment) {
        paymentJpaRepository.save(payment.entity)
    }

    private val PaymentEntity.domain: Payment
        get() =
            Payment(
                id = id,
                userId = userId,
                status = paymentStatus,
                paymentMethod = paymentMethod,
                paymentInfos =
                    paymentInfos.map {
                        PaymentInfo(
                            id = it.id,
                            performancePriceId = it.performancePriceId,
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
                paymentStatus = status,
                paymentMethod = paymentMethod,
                paymentInfos =
                    paymentInfos.map {
                        PaymentInfoEntity(
                            id = it.id,
                            paymentId = id,
                            performancePriceId = it.performancePriceId,
                            reservationCount = it.reservationCount,
                            discountId = it.discountId,
                            paymentAmount = it.paymentAmount.amount,
                        )
                    },
            )
}
