package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.KopisPerformanceCreateRequest
import com.hunhui.ticketworld.application.dto.response.DummyPerformanceCreateResponse
import com.hunhui.ticketworld.domain.kopis.KopisPerformance
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceFacility
import com.hunhui.ticketworld.domain.kopis.KopisPerformanceGenre
import com.hunhui.ticketworld.domain.kopis.KopisRepository
import com.hunhui.ticketworld.domain.kopis.PerformanceSchedule
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PerformanceInfo
import com.hunhui.ticketworld.domain.performance.PerformanceRepository
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.seatarea.SeatArea
import com.hunhui.ticketworld.domain.seatarea.SeatAreaRepository
import com.hunhui.ticketworld.domain.seatarea.SeatPosition
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import com.hunhui.ticketworld.domain.seatgrade.SeatGradeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.sqrt

@Service
class DummyPerformanceService(
    private val performanceRepository: PerformanceRepository,
    private val seatAreaRepository: SeatAreaRepository,
    private val seatGradeRepository: SeatGradeRepository,
    private val kopisRepository: KopisRepository,
) {
    private val logger = LogFactory.getLog(DummyPerformanceService::class.java)

    private val maxConcurrentAsyncTasks = 10

    private enum class ProcessStatus {
        SKIPPED,
        SUCCESS,
        FAILED,
    }

    /**
     * Kopis API를 통해 조회한 공연 ID 목록을 기반으로
     * 각 공연을 병렬(비동기) 처리하여 저장합니다.
     * supervisorScope를 사용하여 개별 작업의 실패가 전체 작업에 영향을 주지 않도록 합니다.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun createDummyPerformancesByKopis(request: KopisPerformanceCreateRequest): DummyPerformanceCreateResponse =
        supervisorScope {
            // blocking 호출이므로 IO 디스패처 사용
            val kopisIds: List<String> =
                withContext(Dispatchers.IO) {
                    kopisRepository.findPerformanceIds(
                        currentPage = request.currentPage,
                        rows = request.rows,
                        startDate = request.startDate,
                        endDate = request.endDate,
                        openRun = request.openRun,
                        kopisPerformanceGenre = request.kopisPerformanceGenre,
                    )
                }
            logger.info("총 ${kopisIds.size}개의 공연 조회 완료")

            // 동시 실행 작업 수 제한: limitedParallelism를 사용하여 최대 동시 작업 수를 제한합니다.
            val limitedDispatcher = Dispatchers.IO.limitedParallelism(maxConcurrentAsyncTasks)

            // 각 공연 ID에 대해 비동기 작업을 수행 (각 작업 내에서 try-catch로 모든 예외를 처리)
            val results: List<ProcessStatus> =
                kopisIds
                    .map { id ->
                        async(limitedDispatcher) {
                            try {
                                if (performanceRepository.findByKopisId(id) != null) {
                                    logger.info("이미 존재하는 공연 스킵 (id: $id)")
                                    return@async ProcessStatus.SKIPPED
                                } else {
                                    logger.info("공연 처리 시작 (id: $id)")
                                    processPerformance(id)
                                }
                            } catch (e: Exception) {
                                logger.error("공연 처리 실패 (id: $id)", e)
                                return@async ProcessStatus.FAILED
                            }
                        }
                    }.awaitAll()

            DummyPerformanceCreateResponse(
                skippedCount = results.count { it == ProcessStatus.SKIPPED },
                successCount = results.count { it == ProcessStatus.SUCCESS },
                failedCount = results.count { it == ProcessStatus.FAILED },
            )
        }

    /**
     * 하나의 공연 처리:
     *  - KopisPerformance 및 시설정보 조회
     *  - 좌석 수 유효성 체크
     *  - 유효 기간 내 회차 생성
     *  - Performance, SeatGrade, SeatArea 생성 후 저장
     */
    private fun processPerformance(id: String): ProcessStatus {
        val kopisPerformance: KopisPerformance = kopisRepository.getPerformanceById(id)
        val facilityId = kopisPerformance.facilityId
        val facility: KopisPerformanceFacility = kopisRepository.getPerformanceFacilityById(facilityId)

        val seatScale: Int? = facility.getSeatScaleByFullName(kopisPerformance.location)
        if (seatScale == null || seatScale == 0) {
            logger.info("좌석 정보 없음 (id: $id, seatScale: $seatScale)")
            return ProcessStatus.SKIPPED
        }

        val scheduleDates =
            getScheduleDates(
                kopisPerformance.startDate,
                kopisPerformance.endDate,
                kopisPerformance.schedules,
            )
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
            return ProcessStatus.SKIPPED
        }
        val seatAreas =
            createSeatAreas(
                performanceId = performance.id,
                seatScale = seatScale,
                seatGradeIds = seatGrades.map { it.id },
            )

        logger.info("\n제목:\t${kopisPerformance.title}\n회차 수:\t${rounds.size}")
        performanceRepository.save(performance)
        seatGradeRepository.saveAll(seatGrades)
        seatAreaRepository.saveAll(seatAreas)

        logger.info("공연 처리 완료 (id: $id)")
        return ProcessStatus.SUCCESS
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
            locationAddress = facility.address,
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
