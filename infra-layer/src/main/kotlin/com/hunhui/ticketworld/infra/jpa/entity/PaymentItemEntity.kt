package com.hunhui.ticketworld.infra.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "payment_item")
internal class PaymentItemEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "payment_id", nullable = false)
    val paymentId: UUID,
    @Column(name = "seat_grade_name", nullable = false)
    val seatGradeName: String,
    @Column(name = "reservation_count", nullable = false)
    val reservationCount: Int,
    @Column(name = "discount_name", nullable = false)
    val discountName: String,
    @Column(name = "original_price", nullable = false)
    val originalPrice: Long,
    @Column(name = "discounted_price", nullable = false)
    val discountedPrice: Long,
) : BaseTimeEntity()
