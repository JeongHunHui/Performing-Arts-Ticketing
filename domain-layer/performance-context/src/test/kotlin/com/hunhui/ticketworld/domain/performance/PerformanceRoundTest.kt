package com.hunhui.ticketworld.domain.performance

import com.hunhui.ticketworld.domain.performance.exception.InvalidPerformanceRoundException
import com.hunhui.ticketworld.domain.performance.exception.PerformanceRoundErrorCode
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID

class PerformanceRoundTest {
    @Test
    fun `유효한 정보로 PerformanceRound 생성이 가능하다`() {
        // uuid Mocking
        val uuid = UUID.randomUUID()
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns uuid

        // given
        val now = LocalDateTime.now()
        val round =
            PerformanceRound.create(
                performanceDateTime = now.plusDays(2),
                reservationStartDateTime = now,
                reservationFinishDateTime = now.plusDays(1),
            )

        // then
        assertEquals(uuid, round.id)
        assertTrue(round.performanceDateTime.isAfter(now))
        assertTrue(round.reservationStartDateTime.isBefore(round.reservationFinishDateTime))
    }

    @Test
    fun `예매 시작 시간이 예매 종료 시간보다 늦으면 예외가 발생한다`() {
        // given
        val now = LocalDateTime.now()

        // when & then
        val exception =
            assertThrows<InvalidPerformanceRoundException> {
                PerformanceRound.create(
                    performanceDateTime = now.plusDays(1),
                    // 일부러 늦게 설정
                    reservationStartDateTime = now.plusDays(2),
                    reservationFinishDateTime = now.plusDays(1),
                )
            }
        assertEquals(PerformanceRoundErrorCode.RESERVATION_START_DATE_IS_AFTER_FINISH_DATE, exception.errorCode)
    }

    @Test
    fun `예매 종료 시간이 공연 시작 시간보다 늦으면 예외가 발생한다`() {
        // given
        val now = LocalDateTime.now()

        // when & then
        val exception =
            assertThrows<InvalidPerformanceRoundException> {
                PerformanceRound.create(
                    performanceDateTime = now.plusDays(1),
                    reservationStartDateTime = now,
                    // 공연 시간보다 늦음
                    reservationFinishDateTime = now.plusDays(2),
                )
            }
        assertEquals(PerformanceRoundErrorCode.RESERVATION_FINISH_DATE_IS_AFTER_PERFORMANCE_DATE, exception.errorCode)
    }

    @Test
    fun `isReservationAvailable- 현재 시간이 예약 기간 범위 내라면 true를 반환한다`() {
        // given
        val now = LocalDateTime.now()
        val round =
            PerformanceRound.create(
                performanceDateTime = now.plusDays(2),
                reservationStartDateTime = now.minusHours(1),
                reservationFinishDateTime = now.plusHours(1),
            )

        // when
        val result = round.isReservationAvailable

        // then
        assertTrue(result)
    }

    @Test
    fun `isReservationAvailable- 현재 시간이 예약 종료 시간을 지났다면 false를 반환한다`() {
        // given
        val now = LocalDateTime.now()
        val round =
            PerformanceRound.create(
                performanceDateTime = now.plusDays(2),
                reservationStartDateTime = now.minusDays(1),
                // 이미 종료됨
                reservationFinishDateTime = now.minusHours(1),
            )

        // when
        val result = round.isReservationAvailable

        // then
        assertFalse(result)
    }

    @Test
    fun `isReservationAvailable- 현재 시간이 예약 시작 시간 이전이라면 false를 반환한다`() {
        // given
        val now = LocalDateTime.now()
        val round =
            PerformanceRound.create(
                performanceDateTime = now.plusDays(2),
                // 아직 예약 시작시간이 아님
                reservationStartDateTime = now.plusHours(1),
                reservationFinishDateTime = now.plusDays(1),
            )

        // when
        val result = round.isReservationAvailable

        // then
        assertFalse(result)
    }
}
