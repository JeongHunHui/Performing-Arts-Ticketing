package com.hunhui.ticketworld.infra.jpa.entity

import com.hunhui.ticketworld.domain.payment.PaymentMethod
import com.hunhui.ticketworld.domain.payment.PaymentStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "payment")
internal class PaymentEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: PaymentStatus,
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    val method: PaymentMethod,
    @OneToMany(
        mappedBy = "paymentId",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val items: List<PaymentItemEntity> = listOf(),
) : BaseTimeEntity()
