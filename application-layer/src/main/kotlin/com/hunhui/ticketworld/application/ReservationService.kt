package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.ReserveRequest
import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.ReservationListResponse
import com.hunhui.ticketworld.application.dto.response.ReserveResponse
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.discount.Discount
import com.hunhui.ticketworld.domain.discount.DiscountRepository
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import com.hunhui.ticketworld.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReservationService(
    private val performanceRepository: PerformanceRepository,
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
    private val discountRepository: DiscountRepository,
    private val paymentRepository: PaymentRepository,
) {
    fun findAll(
        roundId: UUID,
        areaId: UUID,
    ): ReservationListResponse {
        val ticketList: List<Ticket> = reservationRepository.findTicketsByRoundIdAndAreaId(roundId, areaId)
        return ReservationListResponse.from(ticketList)
    }

    @Transactional
    fun tempReserve(tempReserveRequest: TempReserveRequest) {
        tempReserveRequest.validate()
        val reservation: Reservation = reservationRepository.getByIds(tempReserveRequest.reservationIds)
        reservationRepository.save(reservation.tempReserve(tempReserveRequest.userId))
    }

    @Transactional
    fun reserve(reserveRequest: ReserveRequest): ReserveResponse {
        val reservation: Reservation = reservationRepository.getByIds(reserveRequest.reservationIds)

        val discounts: List<Discount> = discountRepository.findAllByIds(reserveRequest.discountIds)

        val reservationPaymentService =
            ReservationPaymentService(
                discounts = discounts,
                reservation = reservation,
                paymentInfos = reserveRequest.paymentInfos,
            )
        val payment: Payment =
            reservationPaymentService.pay(
                paymentMethod = reserveRequest.paymentMethod,
                userId = reserveRequest.userId,
            )

        val updatedReservation: Reservation =
            reservation.reserve(
                paymentId = payment.id,
                tryReserveUserId = reserveRequest.userId,
            )

        paymentRepository.save(payment)
        reservationRepository.save(updatedReservation)

        return ReserveResponse(payment.id)
    }

    private fun TempReserveRequest.validate() {
        userRepository.getById(userId)
        val reservationCount = performanceRepository.getById(performanceId).reservationCount
        if (reservationCount < reservationIds.size) throw BusinessException(ReservationErrorCode.RESERVATION_COUNT_EXCEED)
    }
}
