package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.TempReserveRequest
import com.hunhui.ticketworld.application.dto.response.TempReserveResponse
import com.hunhui.ticketworld.application.dto.response.TicketListResponse
import com.hunhui.ticketworld.application.repository.FakePerformanceRepository
import com.hunhui.ticketworld.application.repository.FakeReservationRepository
import com.hunhui.ticketworld.common.error.AssertUtil.assertErrorCode
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PerformanceInfo
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.reservation.exception.ReservationErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class ReservationServiceTest {
    @Test
    fun `findAllTickets는 옳바른 티켓 목록 응답을 반환해야한다`() {
        // Given
        val roundId = UUID.randomUUID()
        val areaId = UUID.randomUUID()

        // 예매 가능 & roundId와 areaId가 일치하는 티켓
        val ticket1 =
            Ticket(
                id = UUID.randomUUID(),
                performanceRoundId = roundId,
                seatAreaId = areaId,
                seatPositionId = UUID.randomUUID(),
                seatGradeId = UUID.randomUUID(),
                reservationId = null,
                isPaid = false,
                expireTime = LocalDateTime.now(),
            )
        val ticket2 =
            Ticket(
                id = UUID.randomUUID(),
                performanceRoundId = roundId,
                seatAreaId = areaId,
                seatPositionId = UUID.randomUUID(),
                seatGradeId = UUID.randomUUID(),
                reservationId = UUID.randomUUID(),
                isPaid = true,
                expireTime = LocalDateTime.now().plusMinutes(10),
            )

        // 다른 roundId 혹은 areaId인 티켓 (조회 대상에서 제외되어야 함)
        val ticketOther =
            Ticket(
                id = UUID.randomUUID(),
                performanceRoundId = roundId,
                seatAreaId = UUID.randomUUID(),
                seatPositionId = UUID.randomUUID(),
                seatGradeId = UUID.randomUUID(),
                reservationId = null,
                isPaid = false,
                expireTime = LocalDateTime.now().plusMinutes(10),
            )

        // Fake Repository 생성 및 테스트 데이터 추가
        val fakeReservationRepository =
            FakeReservationRepository().apply {
                addTicket(ticket1)
                addTicket(ticket2)
                addTicket(ticketOther)
            }
        val fakePerformanceRepository = FakePerformanceRepository()

        // ReservationService 인스턴스 생성
        val reservationService =
            ReservationService(
                performanceRepository = fakePerformanceRepository,
                reservationRepository = fakeReservationRepository,
            )

        // When
        val response: TicketListResponse = reservationService.findAllTickets(roundId, areaId)

        // Then
        with(response) {
            val returnedTicketIds = tickets.map { it.id }

            // ticketOther는 roundId가 다르므로 ticket1, ticket2만 조회되어야 한다.
            assertTrue(returnedTicketIds.contains(ticket1.id))
            assertTrue(returnedTicketIds.contains(ticket2.id))
            val responseTicket1 = tickets.first { it.id == ticket1.id }
            val responseTicket2 = tickets.first { it.id == ticket2.id }

            // ticket1은 예매 가능, ticket2는 예매 불가 상태여야 한다.
            assertTrue(responseTicket1.canReserve)
            assertFalse(responseTicket2.canReserve)

            // 나머지 필드 값 확인.
            assertEquals(ticket1.seatPositionId, responseTicket1.seatPositionId)
            assertEquals(ticket2.seatPositionId, responseTicket2.seatPositionId)
        }
    }

    @Test
    fun `임시 예매 정상 동작`() {
        // Given
        val fakePerformanceRepository = FakePerformanceRepository()
        val fakeReservationRepository = FakeReservationRepository()

        val now = LocalDateTime.now()
        // 현재 예약 가능한 회차: reservationStartTime <= now <= reservationEndTime
        val availableRound =
            PerformanceRound(
                id = UUID.randomUUID(),
                roundStartTime = now.plusMinutes(30),
                reservationStartTime = now.minusMinutes(5),
                reservationEndTime = now.plusMinutes(5),
            )
        // maxReservationCount가 충분한 공연 생성
        val performance =
            Performance(
                id = UUID.randomUUID(),
                info =
                    PerformanceInfo(
                        title = "테스트 공연",
                        genre = PerformanceGenre.CONCERT,
                        posterUrl = "http://test.com/image.png",
                        location = "Test Hall",
                    ),
                description = "테스트 설명",
                maxReservationCount = 10,
                rounds = listOf(availableRound),
            )
        fakePerformanceRepository.save(performance)

        // 예약할 티켓 생성 (티켓의 회차는 availableRound.id 사용)
        val ticketId1 = UUID.randomUUID()
        val ticket1 =
            Ticket(
                id = ticketId1,
                performanceRoundId = availableRound.id,
                seatAreaId = UUID.randomUUID(),
                seatPositionId = UUID.randomUUID(),
                seatGradeId = UUID.randomUUID(),
                reservationId = null,
                isPaid = false,
                expireTime = now,
            )
        fakeReservationRepository.addTicket(ticket1)

        val ticketId2 = UUID.randomUUID()
        val ticket2 =
            Ticket(
                id = ticketId2,
                performanceRoundId = availableRound.id,
                seatAreaId = UUID.randomUUID(),
                seatPositionId = UUID.randomUUID(),
                seatGradeId = UUID.randomUUID(),
                reservationId = null,
                isPaid = false,
                expireTime = now,
            )
        fakeReservationRepository.addTicket(ticket2)

        val request =
            TempReserveRequest(
                performanceId = performance.id,
                userId = UUID.randomUUID(),
                ticketIds = listOf(ticketId1, ticketId2),
            )

        val reservationService =
            ReservationService(
                performanceRepository = fakePerformanceRepository,
                reservationRepository = fakeReservationRepository,
            )

        // When
        val response: TempReserveResponse = reservationService.tempReserve(request)

        // Then
        // 저장된 예약 목록 중 response.reservationId와 일치하는 예약이 있어야 함
        assertTrue(fakeReservationRepository.isExists(response.reservationId))
    }

    @Test
    fun `예매 가능 수량 초과 시 예외 발생`() {
        // Given
        val fakePerformanceRepo = FakePerformanceRepository()
        val fakeReservationRepo = FakeReservationRepository()

        val now = LocalDateTime.now()
        // 예약 가능한 회차 생성 (정상 동작하는 회차)
        val availableRound =
            PerformanceRound(
                id = UUID.randomUUID(),
                roundStartTime = now.plusMinutes(30),
                reservationStartTime = now.minusMinutes(5),
                reservationEndTime = now.plusMinutes(5),
            )
        // maxReservationCount를 1로 설정하여 티켓 수가 초과하면 예외가 발생하도록 함
        val performance =
            Performance(
                id = UUID.randomUUID(),
                info =
                    PerformanceInfo(
                        title = "테스트 공연",
                        genre = PerformanceGenre.CONCERT,
                        posterUrl = "http://test.com/image.png",
                        location = "Test Hall",
                    ),
                description = "테스트 설명",
                maxReservationCount = 1,
                rounds = listOf(availableRound),
            )
        fakePerformanceRepo.save(performance)

        // 두 개의 티켓을 예약 요청 (2 > 1)
        val ticketId1 = UUID.randomUUID()
        val ticket1 =
            Ticket(
                id = ticketId1,
                performanceRoundId = availableRound.id,
                seatAreaId = UUID.randomUUID(),
                seatPositionId = UUID.randomUUID(),
                seatGradeId = UUID.randomUUID(),
                reservationId = null,
                isPaid = false,
                expireTime = now.plusMinutes(10),
            )
        fakeReservationRepo.addTicket(ticket1)

        val ticketId2 = UUID.randomUUID()
        val ticket2 =
            Ticket(
                id = ticketId2,
                performanceRoundId = availableRound.id,
                seatAreaId = UUID.randomUUID(),
                seatPositionId = UUID.randomUUID(),
                seatGradeId = UUID.randomUUID(),
                reservationId = null,
                isPaid = false,
                expireTime = now.plusMinutes(10),
            )
        fakeReservationRepo.addTicket(ticket2)

        val request =
            TempReserveRequest(
                performanceId = performance.id,
                userId = UUID.randomUUID(),
                ticketIds = listOf(ticketId1, ticketId2),
            )

        val reservationService =
            ReservationService(
                performanceRepository = fakePerformanceRepo,
                reservationRepository = fakeReservationRepo,
            )

        // When & Then: 예매 가능 수량 초과 시 BusinessException이 발생해야 함
        assertErrorCode(ReservationErrorCode.RESERVATION_COUNT_EXCEED) {
            reservationService.tempReserve(
                request,
            )
        }
    }

    @Test
    fun `예약 가능한 회차가 아닐 경우 예외 발생`() {
        // Given
        val fakePerformanceRepo = FakePerformanceRepository()
        val fakeReservationRepo = FakeReservationRepository()

        val now = LocalDateTime.now()
        // 예약 시간이 이미 지난 회차 생성 (예약 불가능)
        val notAvailableRound =
            PerformanceRound(
                id = UUID.randomUUID(),
                roundStartTime = now.plusMinutes(30),
                reservationStartTime = now.minusMinutes(20),
                reservationEndTime = now.minusMinutes(10),
            )
        val performance =
            Performance(
                id = UUID.randomUUID(),
                info =
                    PerformanceInfo(
                        title = "테스트 공연",
                        genre = PerformanceGenre.CONCERT,
                        posterUrl = "http://test.com/image.png",
                        location = "Test Hall",
                    ),
                description = "테스트 설명",
                maxReservationCount = 10,
                rounds = listOf(notAvailableRound),
            )
        fakePerformanceRepo.save(performance)

        // 예약할 티켓 생성 (회차는 notAvailableRound.id 사용)
        val ticketId = UUID.randomUUID()
        val ticket =
            Ticket(
                id = ticketId,
                performanceRoundId = notAvailableRound.id,
                seatAreaId = UUID.randomUUID(),
                seatPositionId = UUID.randomUUID(),
                seatGradeId = UUID.randomUUID(),
                reservationId = null,
                isPaid = false,
                expireTime = now,
            )
        fakeReservationRepo.addTicket(ticket)

        val request =
            TempReserveRequest(
                performanceId = performance.id,
                userId = UUID.randomUUID(),
                ticketIds = listOf(ticketId),
            )

        val reservationService =
            ReservationService(
                performanceRepository = fakePerformanceRepo,
                reservationRepository = fakeReservationRepo,
            )

        // When & Then: 예약 가능한 회차가 아니므로 BusinessException 발생
        assertErrorCode(PerformanceErrorCode.ROUND_NOT_AVAILABLE) {
            reservationService.tempReserve(
                request,
            )
        }
    }
}
