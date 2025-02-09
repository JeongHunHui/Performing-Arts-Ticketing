package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.DummyReservationCreateRequest
import com.hunhui.ticketworld.application.dto.response.DummyReservationCreateResponse
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentMethod
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.seatarea.SeatArea
import com.hunhui.ticketworld.domain.seatarea.SeatAreaRepository
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.random.Random

@Service
class DummyReservationService(
    private val performanceRepository: PerformanceRepository,
    private val seatAreaRepository: SeatAreaRepository,
    private val seatGradeRepository: SeatGradeRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository,
) {
    private val logger = LogFactory.getLog(DummyPerformanceService::class.java)

    /**
     * 각 공연별 예매 생성 작업을 병렬로 처리합니다.
     */
    suspend fun createDummyReservations(request: DummyReservationCreateRequest): DummyReservationCreateResponse =
        coroutineScope {
            // DB 조회는 I/O 디스패처 사용
            val (performances, _) =
                withContext(Dispatchers.IO) {
                    performanceRepository.findAllWithPagenation(request.page, request.size)
                }
            val performanceIds = performances.map { it.id }
            val seatAreasMap: Map<UUID, List<SeatArea>> =
                withContext(Dispatchers.IO) {
                    seatAreaRepository.findAllByPerformanceIds(performanceIds)
                }
            val seatGradesMap: Map<UUID, List<SeatGrade>> =
                withContext(Dispatchers.IO) {
                    seatGradeRepository.findAllByPerformanceIds(performanceIds)
                }
            logger.info("총 ${performances.size}개의 공연 조회 완료")

            // 예약 관련 설정 (요청 파라미터에 없으면 기본값 사용)
            val reservationRatio = request.reservationRatio
            val minTicketsPerReservation = request.minTicketsPerReservation
            val maxTicketsPerReservation = request.maxTicketsPerReservation

            // 각 공연별로 예매 생성 작업을 병렬로 실행
            val results =
                performances
                    .map { performance ->
                        async(Dispatchers.IO) {
                            try {
                                logger.info("예매 생성 시작 (id: ${performance.id})")
                                val targetRounds = performance.rounds.filter { !it.isTicketCreated }
                                // 이미 처리한 회차는 재처리하지 않도록 플래그 설정
                                performance.rounds.forEach { it.isTicketCreated = true }

                                val seatAreas: List<SeatArea> = seatAreasMap[performance.id] ?: emptyList()
                                val tickets = createTickets(seatAreas, targetRounds)
                                val seatGrades: List<SeatGrade> = seatGradesMap[performance.id] ?: emptyList()
                                val (reservations, payments) =
                                    createReservationsAndPayments(
                                        performance,
                                        seatGrades,
                                        tickets,
                                        reservationRatio,
                                        minTicketsPerReservation,
                                        maxTicketsPerReservation,
                                    )

                                // DB 저장 작업도 I/O 디스패처에서 실행
                                withContext(Dispatchers.IO) {
                                    performanceRepository.save(performance)
                                    reservationRepository.saveNewTickets(tickets)
                                    reservationRepository.saveAll(reservations)
                                    paymentRepository.saveAll(payments)
                                }
                                logger.info("예매 생성 완료 (id: ${performance.id})")
                                true
                            } catch (e: Exception) {
                                logger.error("예매 생성 실패 (id: ${performance.id})", e)
                                false
                            }
                        }
                    }.awaitAll()

            val successCount = results.count { it }
            logger.info("총 ${performances.size}개의 공연 중 ${successCount}건 저장 완료")
            DummyReservationCreateResponse(
                processedCount = performances.size,
                successCount = successCount,
            )
        }

    /**
     * 각 회차+구역별 티켓 그룹에 대해 reservationRatio 비율만큼 티켓을 예약합니다.
     *
     * @param reservationRatio 예약할 티켓 비율 (예: 0.7)
     * @param minTicketsPerReservation 그룹 당 최소 티켓 수
     * @param maxTicketsPerReservation 그룹 당 최대 티켓 수
     */
    private fun createReservationsAndPayments(
        performance: Performance,
        seatGrades: List<SeatGrade>,
        tickets: List<Ticket>,
        reservationRatio: Double,
        minTicketsPerReservation: Int,
        maxTicketsPerReservation: Int,
    ): Pair<List<Reservation>, List<Payment>> {
        val reservations = mutableListOf<Reservation>()
        val payments = mutableListOf<Payment>()

        // 각 티켓은 performanceRoundId와 seatAreaId를 가지므로, 이 두 값의 조합으로 그룹핑합니다.
        val ticketsByRoundAndArea: Map<Pair<UUID, UUID>, List<Ticket>> =
            tickets.groupBy { it.performanceRoundId to it.seatAreaId }

        ticketsByRoundAndArea.forEach { (_, areaTickets) ->
            // 각 그룹(회차+구역)에서 reservationRatio 비율만큼 예매 대상으로 처리
            val totalTicketsToReserve = (areaTickets.size * reservationRatio).toInt()
            val remainingTickets = areaTickets.take(totalTicketsToReserve).toMutableList()
            while (remainingTickets.isNotEmpty()) {
                // minTicketsPerReservation ~ maxTicketsPerReservation 사이의 티켓 수를 무작위로 결정
                val groupSize = Random.nextInt(minTicketsPerReservation, maxTicketsPerReservation + 1)
                val numTicketsToReserve = minOf(groupSize, remainingTickets.size)
                // 그룹에 포함될 티켓들을 추출 후 제거
                val groupTickets = remainingTickets.subList(0, numTicketsToReserve).toList()
                repeat(numTicketsToReserve) { remainingTickets.removeAt(0) }

                val userId = UUID.randomUUID()
                // 모든 티켓은 동일 performanceRound에 속한다고 가정
                val reservation =
                    Reservation.createTempReservation(
                        tickets = groupTickets,
                        userId = userId,
                        performanceId = performance.id,
                    )
                val payment = Payment.create(userId = userId, paymentMethod = PaymentMethod.CREDIT_CARD)

                // 그룹 내 티켓들을 좌석등급별로 묶어 결제 항목 추가
                groupTickets.groupBy { it.seatGradeId }.forEach { (seatGradeId, ticketsGroup) ->
                    val seatGrade = seatGrades.first { it.id == seatGradeId }
                    val reservationCount = ticketsGroup.size
                    val (discountName, originalPrice, discountedPrice) =
                        seatGrade.calculatePaymentAmount(
                            discountId = null,
                            reservationCount = reservationCount,
                        )
                    payment.addItem(
                        seatGradeName = seatGrade.name,
                        reservationCount = reservationCount,
                        discountName = discountName,
                        originalPrice = originalPrice,
                        discountedPrice = discountedPrice,
                    )
                }
                // 예매 확정 및 결제 완료 처리
                reservations.add(reservation.confirm(userId, payment.id))
                payments.add(payment.complete())
            }
        }
        return reservations to payments
    }

    // 각 구역의 좌석과 회차 정보를 기반으로 Ticket 생성
    private fun createTickets(
        seatAreas: List<SeatArea>,
        rounds: List<PerformanceRound>,
    ): List<Ticket> =
        seatAreas.flatMap { seatArea ->
            seatArea.positions.flatMap { seat ->
                rounds.map { round ->
                    Ticket.create(
                        performanceRoundId = round.id,
                        seatAreaId = seatArea.id,
                        seatPositionId = seat.id,
                        seatGradeId = seat.seatGradeId,
                    )
                }
            }
        }
}
