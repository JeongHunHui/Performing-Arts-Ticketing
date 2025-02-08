package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.KopisPerformanceCreateRequest
import com.hunhui.ticketworld.application.dto.response.DummyDateCreateResponse
import com.hunhui.ticketworld.domain.kopis.KopisPerformance
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceFacility
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceGenre
import com.hunhui.ticketworld.domain.kopis.KopisRepository
import com.hunhui.ticketworld.domain.kopis.PerformanceSchedule
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentMethod
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PerformanceInfo
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.seatarea.SeatArea
import com.hunhui.ticketworld.domain.seatarea.SeatAreaRepository
import com.hunhui.ticketworld.domain.seatarea.SeatPosition
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.random.Random

@Service
class DummyDataService(
    private val performanceRepository: PerformanceRepository,
    private val seatAreaRepository: SeatAreaRepository,
    private val seatGradeRepository: SeatGradeRepository,
    private val reservationRepository: ReservationRepository,
    private val kopisRepository: KopisRepository,
    private val paymentRepository: PaymentRepository,
) {
    private val logger = LogFactory.getLog(DummyDataService::class.java)

    fun createDummyData(request: KopisPerformanceCreateRequest): DummyDateCreateResponse {
        val kopisIds: List<String> =
            kopisRepository.findPerformanceIds(
                currentPage = request.currentPage,
                rows = request.rows,
                startDate = request.startDate,
                endDate = request.endDate,
                openRun = request.openRun,
                kopisPerformanceGenre = request.kopisPerformanceGenre,
            )
        logger.info("총 ${kopisIds.size}개의 공연 조회 완료")

        var processedCount = 0
        var successCount = 0
        for (id in kopisIds) {
            if (performanceRepository.findByKopisId(id) != null) {
                logger.info("이미 존재하는 공연 스킵 (id: $id)")
                continue
            }
            try {
                processedCount++
                logger.info("[$processedCount/${kopisIds.size}] 공연 처리 시작 (id: $id)")
                val isSuccess = processPerformance(id, request)
                if (isSuccess) successCount++
            } catch (e: Exception) {
                logger.error("공연 처리 실패 (id: $id)", e)
            }
        }
        logger.info("총 ${kopisIds.size}개의 공연 중 ${successCount}건 저장 완료")
        return DummyDateCreateResponse(
            processedCount = processedCount,
            successCount = successCount,
        )
    }

    /**
     * 하나의 공연 처리:
     *  - KopisPerformance, 시설정보 조회
     *  - 좌석 수 유효성 체크
     *  - 유효 기간 내 회차 생성
     *  - Performance, SeatGrade, SeatArea, Ticket, Reservation, Payment 생성 후 저장
     */
    private fun processPerformance(
        id: String,
        request: KopisPerformanceCreateRequest,
    ): Boolean {
        val kopisPerformance = kopisRepository.getPerformanceById(id)
        val facilityId = kopisPerformance.facilityId
        val facility = kopisRepository.getPerformanceFacilityById(facilityId)

        val seatScale: Int? = facility.getSeatScaleByFullName(kopisPerformance.location)
        if (seatScale == null || seatScale == 0) {
            logger.info("좌석 정보 없음 (id: $id, seatScale: $seatScale)")
            return false
        }

        // 유효 기간 결정: 두 날짜 중 더 늦은 시작일, 더 이른 종료일
        val effectiveStart = maxOf(kopisPerformance.startDate, request.startDate)
        val effectiveEnd = minOf(kopisPerformance.endDate, request.endDate)

        val scheduleDates = getScheduleDates(effectiveStart, effectiveEnd, kopisPerformance.schedules)
        val rounds = createPerformanceRounds(scheduleDates)
        val performanceInfo = buildPerformanceInfo(kopisPerformance, facility)
        val performance =
            Performance.create(
                performanceInfo = performanceInfo,
                description = "설명",
                rounds = rounds,
                maxReservationCount = 10,
            )

        val seatGrades = createSeatGrades(kopisPerformance, performance.id)

        if (seatGrades.isEmpty()) {
            logger.info("가격 정보 없음 (id: $id)")
            return false
        }
        val seatAreas =
            createSeatAreas(
                performanceId = performance.id,
                seatScale = seatScale,
                seatGradeIds = seatGrades.map { it.id },
            )
        val tickets = createTickets(seatAreas, rounds)
        val (reservations, payments) = createReservationsAndPayments(performance, seatGrades, tickets)
        logger.info("\n제목:\t${kopisPerformance.title}\n회차 수:\t${rounds.size}\n티켓 수:\t${tickets.size}\n예매 수:\t${reservations.size}")

        performanceRepository.save(performance)
        seatGradeRepository.saveAll(seatGrades)
        seatAreaRepository.saveAll(seatAreas)
        reservationRepository.saveNewTickets(tickets)
        reservationRepository.saveAll(reservations)
        paymentRepository.saveAll(payments)

        logger.info("공연 처리 완료 (id: $id)")
        return true
    }

    private fun createReservationsAndPayments(
        performance: Performance,
        seatGrades: List<SeatGrade>,
        tickets: List<Ticket>,
    ): Pair<List<Reservation>, List<Payment>> {
        val reservations = mutableListOf<Reservation>()
        val payments = mutableListOf<Payment>()

        // 회차별 티켓 그룹핑: key는 performanceRoundId
        val ticketsByRound: Map<UUID, List<Ticket>> = tickets.groupBy { it.performanceRoundId }

        ticketsByRound.forEach { (_, roundTickets) ->
            // 각 회차 그룹에서 70%만 예약 대상으로 처리
            val totalTicketsToReserve = (roundTickets.size * 0.7).toInt()
            val remainingTickets = roundTickets.take(totalTicketsToReserve).toMutableList()
            while (remainingTickets.isNotEmpty()) {
                // 1~4장 사이의 티켓 수를 무작위로 결정 (남은 티켓 수를 초과하지 않도록)
                val groupSize = Random.nextInt(1, 5)
                val numTicketsToReserve = minOf(groupSize, remainingTickets.size)

                // 그룹에 포함될 티켓들을 추출 후 제거
                val groupTickets = remainingTickets.subList(0, numTicketsToReserve).toList()
                repeat(numTicketsToReserve) { remainingTickets.removeAt(0) }

                val userId = UUID.randomUUID()
                // 생성되는 모든 티켓은 동일 회차에 속하므로 Reservation 내 티켓들이 모두 같은 round id입니다.
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
                // 예약 확정 및 결제 완료 처리
                reservation.confirm(userId, payment.id)
                payment.complete()

                reservations.add(reservation)
                payments.add(payment)
            }
        }
        return reservations to payments
    }

    // PerformanceInfo 생성
    private fun buildPerformanceInfo(
        kopisPerformance: KopisPerformance,
        facility: KopisPerformanceFacility,
    ): PerformanceInfo =
        PerformanceInfo(
            kopisId = kopisPerformance.id,
            title = kopisPerformance.title,
            genre = kopisPerformance.genre.toOriginal(),
            posterUrl = kopisPerformance.posterUrl,
            kopisFacilityId = kopisPerformance.facilityId,
            location = kopisPerformance.location,
            locationAddress = facility.address, // facility.address가 존재한다고 가정
            cast = kopisPerformance.cast,
            crew = kopisPerformance.crew,
            runtime = kopisPerformance.runtime,
            ageLimit = kopisPerformance.ageLimit,
            descriptionImageUrls = kopisPerformance.descriptionImageUrls,
        )

    // 회차 생성: 각 공연일시에 대해 PerformanceRound 생성
    private fun createPerformanceRounds(dates: List<LocalDateTime>): List<PerformanceRound> =
        dates.map { date ->
            PerformanceRound.create(
                roundStartTime = date,
                reservationStartTime = date.minusDays(30),
                reservationEndTime = date.minusDays(1),
            )
        }

    // KopisPerformance의 좌석등급 정보를 기반으로 SeatGrade 생성
    private fun createSeatGrades(
        kopisPerformance: KopisPerformance,
        performanceId: UUID,
    ): List<SeatGrade> =
        kopisPerformance.seatGradeInfos.map { seatGradeInfo ->
            SeatGrade.create(
                performanceId = performanceId,
                name = seatGradeInfo.name,
                price = seatGradeInfo.price.amount,
            )
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

    // 공연 일정 생성: 주어진 기간과 스케줄 정보를 기반으로 LocalDateTime 목록 생성
    private fun getScheduleDates(
        startDate: LocalDate,
        endDate: LocalDate,
        schedules: List<PerformanceSchedule>,
    ): List<LocalDateTime> {
        val dates = mutableListOf<LocalDateTime>()
        var date = startDate
        while (!date.isAfter(endDate)) {
            if (isHoliday(date)) {
                schedules
                    .filter { it.day is PerformanceSchedule.Day.Holiday }
                    .forEach { schedule ->
                        schedule.times.forEach { time ->
                            dates.add(LocalDateTime.of(date, time))
                        }
                    }
            } else {
                schedules
                    .filter { it.day is PerformanceSchedule.Day.WeekDays }
                    .forEach { schedule ->
                        val weekDays = (schedule.day as PerformanceSchedule.Day.WeekDays).days
                        if (weekDays.contains(date.dayOfWeek)) {
                            schedule.times.forEach { time ->
                                dates.add(LocalDateTime.of(date, time))
                            }
                        }
                    }
            }
            date = date.plusDays(1)
        }
        return dates
    }

    // TODO: 공휴일 여부 판단
    private fun isHoliday(date: LocalDate): Boolean = false

    // -------------------- 좌석 구역 생성 관련 --------------------

    /**
     * 좌석 총 수(seatScale)와 좌석등급 ID 목록(seatGradeIds)을 받아,
     * 구역(SeatArea) 목록을 생성합니다.
     *
     * 한 구역당 최대 200석이며, 전체 구역은 다음과 같이 명명됩니다.
     *
     * 예)
     *   1층 A-1구역 ~ 1층 Z-1구역
     *   2층 A-1구역 ~ 2층 Z-1구역
     *   3층 A-1구역 ~ 3층 Z-1구역
     *   1층 A-2구역 ~ 1층 Z-2구역, etc.
     */
    private fun createSeatAreas(
        performanceId: UUID,
        seatScale: Int,
        seatGradeIds: List<UUID>,
    ): List<SeatArea> {
        // 1. 총 구역 수 계산 (한 구역 당 최대 200석)
        val totalZones = ceil(seatScale.toDouble() / 200).toInt()

        // 2. 각 구역에 배정할 좌석 수 목록 생성
        val seatCounts = mutableListOf<Int>()
        var remainingSeats = seatScale
        repeat(totalZones) {
            val seatsInArea = if (remainingSeats >= 200) 200 else remainingSeats
            seatCounts.add(seatsInArea)
            remainingSeats -= seatsInArea
        }

        // 3. 구역 이름 생성을 위한 층과 그룹 결정
        //    총 구역 수가 26개 이하이면 1층, 아니면 기본적으로 3층 사용
        val floorCount = if (totalZones <= 26) 1 else 3
        val maxZonesPerGroup = floorCount * 26
        val groupCount = ceil(totalZones.toDouble() / maxZonesPerGroup).toInt()

        // 4. (floor, group, letter) 순서로 구역 이름 목록 생성
        val zoneNames = mutableListOf<Pair<String, String>>() // Pair<floorName, areaName>
        for (group in 1..groupCount) {
            for (floor in 1..floorCount) {
                for (letter in 'A'..'Z') {
                    zoneNames.add(Pair("${floor}층", "$letter-${group}구역"))
                }
            }
        }
        val assignedNames = zoneNames.take(totalZones)

        // 5. 각 구역 생성
        return (0 until totalZones).map { i ->
            createSeatArea(
                performanceId = performanceId,
                floorName = assignedNames[i].first,
                areaName = assignedNames[i].second,
                seatCount = seatCounts[i],
                seatGradeIds = seatGradeIds,
            )
        }
    }

    /**
     * 하나의 SeatArea를 생성합니다.
     *
     * 좌석 배치는 좌석 수에 따라 그리드를 계산하며,
     * 좌석번호는 "n열 m번" (n: 행, m: 열) 형식으로 부여되고,
     * 좌석등급 ID는 전달받은 목록을 순환(round-robin) 방식으로 할당합니다.
     */
    private fun createSeatArea(
        performanceId: UUID,
        floorName: String,
        areaName: String,
        seatCount: Int,
        seatGradeIds: List<UUID>,
    ): SeatArea {
        val width = ceil(sqrt(seatCount.toDouble())).toInt()
        val height = ceil(seatCount.toDouble() / width).toInt()

        val positions =
            (0 until seatCount).map { index ->
                val x = index % width
                val y = index / width
                SeatPosition(
                    id = UUID.randomUUID(),
                    seatGradeId = seatGradeIds[index % seatGradeIds.size],
                    number = "${y + 1}열 ${x + 1}번",
                    x = x,
                    y = y,
                )
            }

        return SeatArea(
            id = UUID.randomUUID(),
            performanceId = performanceId,
            floorName = floorName,
            areaName = areaName,
            width = width,
            height = height,
            positions = positions,
        )
    }

    private fun KopisPerformanceGenre.toOriginal(): PerformanceGenre =
        when (this) {
            KopisPerformanceGenre.CIRCUS -> PerformanceGenre.CIRCUS
            KopisPerformanceGenre.DANCING -> PerformanceGenre.DANCING
            KopisPerformanceGenre.MUSICAL -> PerformanceGenre.MUSICAL
            KopisPerformanceGenre.THEATER -> PerformanceGenre.THEATER
            KopisPerformanceGenre.COMPOSITE -> PerformanceGenre.COMPOSITE
            KopisPerformanceGenre.CONCERT -> PerformanceGenre.CONCERT
            KopisPerformanceGenre.CLASSIC -> PerformanceGenre.CLASSIC
            KopisPerformanceGenre.TRADITIONAL -> PerformanceGenre.TRADITIONAL
            KopisPerformanceGenre.PUBLIC_DANCING -> PerformanceGenre.PUBLIC_DANCING
        }
}
