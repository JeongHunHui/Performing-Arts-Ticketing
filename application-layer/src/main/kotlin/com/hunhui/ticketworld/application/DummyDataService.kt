package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.KopisPerformanceCreateRequest
import com.hunhui.ticketworld.domain.kopis.KopisRepository
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.seatarea.SeatAreaRepository
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import com.hunhui.ticketworld.domain.user.UserRepository
import org.springframework.stereotype.Service

@Service
class DummyDataService(
    private val performanceRepository: PerformanceRepository,
    private val seatAreaRepository: SeatAreaRepository,
    private val seatGradeRepository: SeatGradeRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
    private val kopisRepository: KopisRepository,
) {
    fun createDummyData(request: KopisPerformanceCreateRequest) {
        val kopisPerformanceIds: List<String> =
            kopisRepository.findPerformanceIds(
                currentPage = request.currentPage,
                rows = request.rows,
                startDate = request.startDate,
                endDate = request.endDate,
                openRun = request.openRun,
                kopisPerformanceGenre = request.kopisPerformanceGenre,
            )

        for (id in kopisPerformanceIds) {
            val kopisPerformance = kopisRepository.getPerformanceById(id)
            val facilityId = kopisPerformance.facilityId
            val kopisPerformanceFacility = TODO("DB에서 공연시설 조회") ?: kopisRepository.getPerformanceFacilityById(facilityId)
            // TODO: kopisPerformance와 kopisPerformanceFacility를 이용하여 Performance, SeatArea, SeatGrade, Reservation, Payment, User 생성
        }
    }
}
