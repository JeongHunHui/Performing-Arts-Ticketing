package com.hunhui.ticketworld.batch

import com.hunhui.ticketworld.domain.payment.PaymentCount
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.reservationstatistics.ReservationStatisticsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class UpdateReservationStatisticsServiceTest {
    private val paymentRepository: PaymentRepository = mockk()

    private val reservationStatisticsRepository: ReservationStatisticsRepository = mockk(relaxed = true)
    private val service = UpdateReservationStatisticsService(paymentRepository, reservationStatisticsRepository)

    @Test
    fun `updateReservationStatistics should save reservation statistics based on payment counts`() {
        val previousStandardTime = LocalDateTime.of(2025, 3, 26, 10, 30)
        val standardTime = LocalDateTime.of(2025, 3, 26, 11, 30)

        val paymentCounts =
            listOf(
                PaymentCount(
                    performanceId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    standardTime = standardTime,
                    count = 100L,
                ),
                PaymentCount(
                    performanceId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    standardTime = standardTime,
                    count = 200L,
                ),
            )
        every { paymentRepository.getPaymentCountsByTimeRange(previousStandardTime, standardTime) } returns paymentCounts

        service.updateReservationStatistics(previousStandardTime, standardTime)

        verify {
            reservationStatisticsRepository.saveAll(
                match { list ->
                    list.size == 2 &&
                        list.any {
                            it.performanceId == UUID.fromString("00000000-0000-0000-0000-000000000001") &&
                                it.count == 100L &&
                                it.standardTime == standardTime
                        } &&
                        list.any {
                            it.performanceId == UUID.fromString("00000000-0000-0000-0000-000000000002") &&
                                it.count == 200L &&
                                it.standardTime == standardTime
                        }
                },
            )
        }
    }
}
