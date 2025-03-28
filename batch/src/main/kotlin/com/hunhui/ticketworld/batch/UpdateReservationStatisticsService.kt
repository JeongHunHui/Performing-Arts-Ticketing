package com.hunhui.ticketworld.batch

import com.hunhui.ticketworld.domain.payment.PaymentCount
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.reservationstatistics.ReservationStatistics
import com.hunhui.ticketworld.domain.reservationstatistics.ReservationStatisticsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UpdateReservationStatisticsService(
    private val paymentRepository: PaymentRepository,
    private val reservationStatisticsRepository: ReservationStatisticsRepository,
) {
    @Transactional
    fun updateReservationStatistics(
        previousStandardTime: LocalDateTime,
        standardTime: LocalDateTime,
    ) {
        val paymentCounts: List<PaymentCount> = paymentRepository.getPaymentCountsByTimeRange(previousStandardTime, standardTime)
        val reservationStatistics =
            paymentCounts.map {
                ReservationStatistics(
                    performanceId = it.performanceId,
                    standardTime = standardTime,
                    count = it.count,
                )
            }
        reservationStatisticsRepository.saveAll(reservationStatistics)
    }
}
