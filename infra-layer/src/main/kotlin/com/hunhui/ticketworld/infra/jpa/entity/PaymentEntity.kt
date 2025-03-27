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
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment")
internal class PaymentEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(name = "performance_id", nullable = false)
    val performanceId: UUID,
    @Column(name = "performance_round_id", nullable = false)
    val performanceRoundId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: PaymentStatus,
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    val method: PaymentMethod,
    @Column(name = "paid_at", nullable = true)
    val paidAt: LocalDateTime?,
    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    @JoinColumn(name = "payment_id")
    val items: List<PaymentItemEntity> = listOf(),
) : BaseTimeEntity()
