package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.ConfirmReserveRequest
import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.ConfirmReserveResponse
import com.hunhui.ticketworld.application.dto.response.ReservationListResponse
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.discount.Discount
import com.hunhui.ticketworld.domain.discount.DiscountRepository
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Reservations
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
        val reservations: Reservations =
            reservationRepository.getReservations(
                ids = tempReserveRequest.reservationIds,
                tryReserveUserId = tempReserveRequest.userId,
            )
        reservationRepository.saveAll(reservations.tempReserve())
    }

    @Transactional
    fun confirmReserve(confirmReserveRequest: ConfirmReserveRequest): ConfirmReserveResponse {
        val reservations =
            reservationRepository.getReservations(
                ids = confirmReserveRequest.reservationIds,
                tryReserveUserId = confirmReserveRequest.userId,
            )

        val discounts: List<Discount> = discountRepository.findAllByIds(confirmReserveRequest.discountIds)

        val reservationPaymentService =
            ReservationPaymentService(
                discounts = discounts,
                reservations = reservations,
                paymentInfos = confirmReserveRequest.paymentInfos,
            )
        val payment: Payment = reservationPaymentService.pay(confirmReserveRequest.paymentMethod)

        val confirmedReservations: Reservations = reservations.confirmReserve(payment.id)

        paymentRepository.save(payment)
        reservationRepository.saveAll(confirmedReservations)

        return ConfirmReserveResponse(paymentId = payment.id)
    }

    private fun TempReserveRequest.validate() {
        userRepository.getById(userId)
        val reservationCount = performanceRepository.getById(performanceId).reservationCount
        if (reservationCount < reservationIds.size) throw BusinessException(ReservationErrorCode.RESERVATION_COUNT_EXCEED)
    }
}
