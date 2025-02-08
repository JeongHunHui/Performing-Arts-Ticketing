package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.KopisPerformanceCreateRequest
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceGenre
import com.hunhui.ticketworld.domain.kopis.KopisRepository
import com.hunhui.ticketworld.domain.kopis.PerformanceSchedule
import com.hunhui.ticketworld.domain.payment.PaymentRepository
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PerformanceInfo
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.reservation.ReservationRepository
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.seatarea.SeatArea
import com.hunhui.ticketworld.domain.seatarea.SeatAreaRepository
import com.hunhui.ticketworld.domain.seatarea.SeatPosition
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import com.hunhui.ticketworld.domain.user.UserRepository
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.sqrt

@Service
class DummyDataService(
    private val performanceRepository: PerformanceRepository,
    private val seatAreaRepository: SeatAreaRepository,
    private val seatGradeRepository: SeatGradeRepository,
    private val reservationRepository: ReservationRepository,
    private val kopisRepository: KopisRepository,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
) {
    private val logger = LogFactory.getLog(DummyDataService::class.java)

    @Transactional
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
        logger.info("공연 ${kopisPerformanceIds.size}개 조회 완료")
        var count = 0

        for (id in kopisPerformanceIds) {
            if (performanceRepository.findByKopisId(id) != null) {
                // TODO: 이미 존재하는 공연에 대한 처리
                logger.info("이미 존재하는 공연 스킵(id: $id)")
                continue
            }
            val kopisPerformance = kopisRepository.getPerformanceById(id)
            val facilityId = kopisPerformance.facilityId
            val kopisPerformanceFacility = kopisRepository.getPerformanceFacilityById(facilityId)
            val seatScale: Int? = kopisPerformanceFacility.getSeatScaleByFullName(kopisPerformance.location)
            if (seatScale == null || seatScale == 0) {
                logger.info("좌석 정보가 없는 공연 스킵(id: $id, seatScale: $seatScale)")
                continue
            }
            count += 1
            logger.info("공연 $count 시작 (id: $id, title: ${kopisPerformance.title})")
            val scheduleDates: List<LocalDateTime> =
                getScheduleDates(
                    startDate = listOf(kopisPerformance.startDate, request.startDate).max(),
                    endDate = listOf(kopisPerformance.endDate, request.endDate).min(),
                    schedules = kopisPerformance.schedules,
                )
            val performanceRounds: List<PerformanceRound> =
                scheduleDates.map {
                    PerformanceRound.create(
                        roundStartTime = it,
                        reservationStartTime = it.minusDays(30),
                        reservationEndTime = it.minusDays(1),
                    )
                }
            val maxReservationCount = 10
            val description = "설명"
            val performance: Performance =
                Performance.create(
                    performanceInfo =
                        with(kopisPerformance) {
                            PerformanceInfo(
                                kopisId = id,
                                title = title,
                                genre = genre.toOriginal(),
                                posterUrl = posterUrl,
                                kopisFacilityId = facilityId,
                                location = location,
                                locationAddress = kopisPerformanceFacility.address,
                                cast = cast,
                                crew = crew,
                                runtime = runtime,
                                ageLimit = ageLimit,
                                descriptionImageUrls = descriptionImageUrls,
                            )
                        },
                    description = description,
                    rounds = performanceRounds,
                    maxReservationCount = maxReservationCount,
                )
            val seatGrades: List<SeatGrade> =
                kopisPerformance.seatGradeInfos.map { seatGradeInfo ->
                    SeatGrade.create(
                        performanceId = performance.id,
                        name = seatGradeInfo.name,
                        price = seatGradeInfo.price.amount,
                    )
                }
            val seatAreas: List<SeatArea> =
                createSeatAreas(
                    performanceId = performance.id,
                    seatScale = seatScale,
                    seatGradeIds = seatGrades.map { it.id },
                )
            val tickets =
                seatAreas.flatMap { seatArea ->
                    seatArea.positions.flatMap { seat ->
                        performanceRounds.map { round ->
                            Ticket.create(
                                performanceRoundId = round.id,
                                seatAreaId = seatArea.id,
                                seatPositionId = seat.id,
                                seatGradeId = seat.seatGradeId,
                            )
                        }
                    }
                }
            performanceRepository.save(performance)
            seatGradeRepository.saveAll(seatGrades)
            seatAreaRepository.saveAll(seatAreas)
            reservationRepository.saveNewTickets(tickets)
            logger.info("공연 $count 완료 (회차 ${performance.rounds.size}개, 티켓 ${tickets.size}개)")
            // TODO: Reservation, Payment, User 생성
        }
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

    private fun getScheduleDates(
        startDate: LocalDate,
        endDate: LocalDate,
        schedules: List<PerformanceSchedule>,
    ): List<LocalDateTime> {
        val result = mutableListOf<LocalDateTime>()
        var date = startDate
        while (!date.isAfter(endDate)) {
            if (isHoliday(date)) {
                schedules.filter { it.day is PerformanceSchedule.Day.Holiday }.forEach { schedule ->
                    schedule.times.forEach { time ->
                        result.add(LocalDateTime.of(date, time))
                    }
                }
            } else {
                schedules.filter { it.day is PerformanceSchedule.Day.WeekDays }.forEach { schedule ->
                    val weekDays = (schedule.day as PerformanceSchedule.Day.WeekDays).days
                    if (weekDays.contains(date.dayOfWeek)) {
                        schedule.times.forEach { time ->
                            result.add(LocalDateTime.of(date, time))
                        }
                    }
                }
            }
            date = date.plusDays(1)
        }

        return result
    }

    // TODO: 공휴일 체크 메서드 구현
    private fun isHoliday(date: LocalDate): Boolean = false

    private fun createSeatAreas(
        performanceId: UUID,
        seatScale: Int,
        seatGradeIds: List<UUID>,
    ): List<SeatArea> {
        if (seatGradeIds.isEmpty()) {
            throw IllegalArgumentException("SeatGradeIds must not be empty")
        }

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
        //  - 총 구역 수가 26 이하이면 1층만 사용, 그 이상이면 기본적으로 3층을 사용
        val floorCount = if (totalZones <= 26) 1 else 3
        // 한 그룹당 구역 개수 = floorCount * 26 (각 층에 A~Z 26구역)
        val maxZonesPerGroup = floorCount * 26
        // 그룹 수 = ceil(totalZones / (floorCount * 26))
        val groupCount = ceil(totalZones.toDouble() / maxZonesPerGroup).toInt()

        // 4. (floor, group, letter) 순서로 구역 이름 목록 생성
        //    순서: 그룹 번호가 증가할수록 뒤에 이어지며, 각 그룹 내에서
        //           1층 A~Z, 2층 A~Z, 3층 A~Z 순으로 생성됩니다.
        val zoneNames = mutableListOf<Pair<String, String>>() // Pair<floorName, areaName>
        for (group in 1..groupCount) {
            for (floor in 1..floorCount) {
                for (letter in 'A'..'Z') {
                    zoneNames.add(Pair("${floor}층", "$letter-${group}구역"))
                }
            }
        }
        // 실제로 필요한 구역 수만큼 이름을 취함
        val assignedNames = zoneNames.take(totalZones)

        // 5. 각 구역 이름과 해당 좌석 수, seatGradeIds를 기반으로 SeatArea 생성
        val areas = mutableListOf<SeatArea>()
        for (i in 0 until totalZones) {
            areas.add(
                createSeatArea(
                    performanceId = performanceId,
                    floorName = assignedNames[i].first,
                    areaName = assignedNames[i].second,
                    seatCount = seatCounts[i],
                    seatGradeIds = seatGradeIds,
                ),
            )
        }
        return areas
    }

    /**
     * 하나의 SeatArea를 생성하는 헬퍼 함수.
     *
     * @param performanceId 해당 공연의 ID
     * @param floorName 구역이 속한 층 (예: "1층")
     * @param areaName 구역 이름 (예: "A-1구역")
     * @param seatCount 이 구역에 배치할 좌석 수
     * @param seatGradeIds 좌석등급 ID 목록; 각 좌석에는 순환 방식으로 할당됨.
     *
     * 좌석 배치는 좌석 수에 맞게 (가로, 세로) 그리드를 계산하고,
     * 좌석번호는 "n열 m번" 형식 (n: 행, m: 열)으로 부여합니다.
     */
    private fun createSeatArea(
        performanceId: UUID,
        floorName: String,
        areaName: String,
        seatCount: Int,
        seatGradeIds: List<UUID>,
    ): SeatArea {
        // 그리드 계산: 가로(열) 길이는 seatCount의 제곱근 올림값,
        // 세로(행) 길이는 seatCount를 가로 길이로 나눈 올림값
        val width = ceil(sqrt(seatCount.toDouble())).toInt()
        val height = ceil(seatCount.toDouble() / width).toInt()

        // 좌석번호는 (행, 열) 순으로 "n열 m번" 형식으로 부여,
        // 좌석등급 ID는 전달받은 목록을 round-robin 방식으로 할당합니다.
        val positions =
            (0 until seatCount).map { index ->
                val x = index % width // 열 (0부터 width-1)
                val y = index / width // 행 (0부터)
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
}
