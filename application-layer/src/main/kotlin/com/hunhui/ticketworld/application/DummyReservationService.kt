package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.DummyReservationCreateRequest
import com.hunhui.ticketworld.application.dto.request.DummyTicketCreateRequest
import com.hunhui.ticketworld.application.dto.response.DummyReservationCreateResponse
import com.hunhui.ticketworld.application.dto.response.DummyTicketCreateResponse
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
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
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

    private val threadCount = 20

    private enum class ProcessStatus {
        SKIPPED,
        SUCCESS,
        FAILED,
    }

    fun createDummyTickets(request: DummyTicketCreateRequest): DummyTicketCreateResponse {
        val performance: Performance = performanceRepository.getById(request.performanceId)
        val seatAreas: List<SeatArea> = seatAreaRepository.findByPerformanceId(request.performanceId)
        val targetRounds = performance.rounds.filter { !it.isTicketCreated }
        val tickets = createTickets(seatAreas, targetRounds)
        val targetRoundIdSet = targetRounds.map { it.id }.toSet()
        performance.rounds.forEach {
            if (it.id in targetRoundIdSet) it.isTicketCreated = true
        }
        performanceRepository.save(performance)
        reservationRepository.saveNewTickets(tickets)
        return DummyTicketCreateResponse(
            roundCount = targetRounds.size,
            ticketsCount = tickets.size,
        )
    }

    fun createDummyReservations(request: DummyReservationCreateRequest): DummyReservationCreateResponse {
        // 작업 시작 시각 기록
        val startTimeMillis = System.currentTimeMillis()

        // 데이터 조회 단계 (동기적으로 처리)
        val (performances, _) = performanceRepository.findAllWithPagenation(request.page, request.size)
        val performanceIds = performances.map { it.id }
        val seatAreasMap: Map<UUID, List<SeatArea>> = seatAreaRepository.findAllByPerformanceIds(performanceIds)
        val seatGradesMap: Map<UUID, List<SeatGrade>> = seatGradeRepository.findAllByPerformanceIds(performanceIds)
        logger.info("총 ${performances.size}개의 공연 조회 완료")

        // 예약 관련 설정 (요청 파라미터에 없으면 기본값 사용)
        val reservationRatio = request.reservationRatio
        val minTicketsPerReservation = request.minTicketsPerReservation
        val maxTicketsPerReservation = request.maxTicketsPerReservation
        val isNoReservation = request.isNoReservation
        val startDate = request.startDate?.atStartOfDay() ?: LocalDateTime.MIN
        val endDate = request.endDate?.atTime(23, 59, 59) ?: LocalDateTime.MAX

        // 누적 통계 변수
        var totalRoundsCount = 0
        var totalTicketsCount = 0
        var totalReservationsCount = 0

        // Executor for asynchronous DB save tasks
        val executor = Executors.newFixedThreadPool(threadCount)
        // List to hold futures for DB saving tasks per performance
        val saveFutures = mutableListOf<CompletableFuture<ProcessStatus>>()

        // 각 공연별로 순차적으로 예매 생성 작업을 실행
        for ((index, performance) in performances.withIndex()) {
            try {
                // 대상 회차 필터링
                val targetRounds =
                    performance.rounds.filter {
                        (!it.isTicketCreated || request.isTicketAlreadyExists) &&
                            it.roundStartTime in startDate..endDate &&
                            LocalDateTime.now().isBefore(it.reservationEndTime)
                    }
                if (targetRounds.isEmpty()) {
                    logger.info("[${index + 1}/${performances.size}] 대상 회차가 존재하지 않음 (id: ${performance.id})")
                    saveFutures.add(CompletableFuture.completedFuture(ProcessStatus.SKIPPED))
                    continue
                }

                // 대상 회차 수 누적
                totalRoundsCount += targetRounds.size

                // 이미 처리한 회차는 재처리하지 않도록 플래그 설정
                val targetRoundIdSet = targetRounds.map { it.id }.toSet()
                performance.rounds.forEach {
                    if (it.id in targetRoundIdSet) it.isTicketCreated = true
                }

                val seatAreas: List<SeatArea> = seatAreasMap[performance.id] ?: emptyList()

                val tickets =
                    if (request.isTicketAlreadyExists) {
                        seatAreas.flatMap { area ->
                            targetRoundIdSet.flatMap { roundId ->
                                reservationRepository.findTicketsByRoundIdAndAreaId(roundId, area.id)
                            }
                        }
                    } else {
                        createTickets(seatAreas, targetRounds)
                    }
                totalTicketsCount += tickets.size

                logger.info(
                    "[${index + 1}/${performances.size}] 대상 회차 ${targetRounds.size}개, 대상 구역 ${seatAreas.size}개, " +
                        "티켓 ${tickets.size} (id: ${performance.id})",
                )

                if (isNoReservation) {
                    // 예약 없이 티켓만 저장하는 경우:
                    // 비동기로 공연 업데이트와 티켓 저장 작업을 순차적으로 실행
                    val future =
                        CompletableFuture.supplyAsync({
                            try {
                                logger.info("[${index + 1}/${performances.size}] 예매 저장 작업 시작 (id: ${performance.id})")
                                performanceRepository.save(performance)
                                reservationRepository.saveNewTickets(tickets)
                                logger.info("[${index + 1}/${performances.size}] 예매 저장 작업 완료 (id: ${performance.id})")
                                ProcessStatus.SUCCESS
                            } catch (e: Exception) {
                                logger.error("[${index + 1}/${performances.size}] 예매 생성 처리 실패 (id: ${performance.id})", e)
                                ProcessStatus.FAILED
                            }
                        }, executor)
                    saveFutures.add(future)
                } else {
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
                    totalReservationsCount += reservations.size

                    // 비동기로 4개의 DB save 작업(공연, 티켓, 예매, 결제)을 순차적으로 실행
                    val future =
                        CompletableFuture.supplyAsync({
                            try {
                                logger.info("[${index + 1}/${performances.size}] 예매 저장 작업 시작 (id: ${performance.id})")
                                performanceRepository.save(performance)
                                reservationRepository.saveAll(reservations)
                                paymentRepository.saveAll(payments)
                                logger.info("[${index + 1}/${performances.size}] 예매 저장 작업 완료 (id: ${performance.id})")
                                ProcessStatus.SUCCESS
                            } catch (e: Exception) {
                                logger.error("[${index + 1}/${performances.size}] 예매 생성 처리 실패 (id: ${performance.id})", e)
                                ProcessStatus.FAILED
                            }
                        }, executor)
                    saveFutures.add(future)
                }
            } catch (e: Exception) {
                logger.error("[${index + 1}/${performances.size}] 예매 생성 처리 실패 (id: ${performance.id})", e)
                saveFutures.add(CompletableFuture.completedFuture(ProcessStatus.FAILED))
            }
        }

        // 모든 공연의 DB save 작업이 완료될 때까지 대기
        CompletableFuture.allOf(*saveFutures.toTypedArray()).join()

        // Collect 각 공연의 ProcessStatus 결과
        val results = saveFutures.map { it.get() }

        // 작업 종료 시각 및 소요 시간 계산
        val endTimeMillis = System.currentTimeMillis()
        val processingTimeMillis = endTimeMillis - startTimeMillis

        executor.shutdown()

        return DummyReservationCreateResponse(
            skippedCount = results.count { it == ProcessStatus.SKIPPED },
            successCount = results.count { it == ProcessStatus.SUCCESS },
            failedCount = results.count { it == ProcessStatus.FAILED },
            processingTimeMillis = processingTimeMillis,
            totalRoundsCount = totalRoundsCount,
            totalTicketsCount = totalTicketsCount,
            totalReservationsCount = totalReservationsCount,
        )
    }

    /**
     * 각 회차+구역별 티켓 그룹에 대해 reservationRatio 비율만큼 티켓을 예약합니다.
     *
     * @param reservationRatio 예약할 티켓 비율
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

        val ticketsByRoundAndArea: Map<Pair<UUID, UUID>, List<Ticket>> =
            tickets.groupBy { it.performanceRoundId to it.seatAreaId }

        ticketsByRoundAndArea.forEach { (_, areaTickets) ->
            val totalTicketsToReserve = (areaTickets.size * reservationRatio).toInt()
            val remainingTickets = areaTickets.take(totalTicketsToReserve).toMutableList()
            while (remainingTickets.isNotEmpty()) {
                val groupSize = Random.nextInt(minTicketsPerReservation, maxTicketsPerReservation + 1)
                val numTicketsToReserve = minOf(groupSize, remainingTickets.size)
                val groupTickets = remainingTickets.subList(0, numTicketsToReserve).toList()
                repeat(numTicketsToReserve) { remainingTickets.removeAt(0) }

                val userId = UUID.randomUUID()
                val reservation =
                    Reservation.createTempReservation(
                        tickets = groupTickets,
                        userId = userId,
                        performanceId = performance.id,
                    )
                val payment =
                    Payment.create(
                        userId = userId,
                        roundId = reservation.roundId,
                        paymentMethod = PaymentMethod.CREDIT_CARD,
                    )

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
                reservations.add(reservation.confirm(userId, payment.id))
                payments.add(payment.complete())
            }
        }
        return reservations to payments
    }

    /**
     * 각 구역의 좌석과 회차 정보를 기반으로 Ticket을 생성합니다.
     */
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
