package com.hunhui.ticketworld.domain.performance

import com.hunhui.ticketworld.common.error.AssertUtil.assertErrorCode
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.INVALID_RESERVATION_FINISH_DATE
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.INVALID_RESERVATION_START_DATE
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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
                roundStartTime = now.plusDays(2),
                reservationStartTime = now,
                reservationEndTime = now.plusDays(1),
            )

        // then
        assertEquals(uuid, round.id)
        assertTrue(round.roundStartTime.isAfter(now))
        assertTrue(round.reservationStartTime.isBefore(round.reservationEndTime))
    }

    @Test
    fun `예매 시작 시간이 예매 종료 시간보다 늦으면 예외가 발생한다`() {
        // given
        val now = LocalDateTime.now()

        // when & then
        assertErrorCode(INVALID_RESERVATION_START_DATE) {
            PerformanceRound.create(
                roundStartTime = now.plusDays(1),
                // 일부러 늦게 설정
                reservationStartTime = now.plusDays(2),
                reservationEndTime = now.plusDays(1),
            )
        }
    }

    @Test
    fun `예매 종료 시간이 공연 시작 시간보다 늦으면 예외가 발생한다`() {
        // given
        val now = LocalDateTime.now()

        // when & then
        assertErrorCode(INVALID_RESERVATION_FINISH_DATE) {
            PerformanceRound.create(
                roundStartTime = now.plusDays(1),
                reservationStartTime = now,
                // 공연 시간보다 늦음
                reservationEndTime = now.plusDays(2),
            )
        }
    }

    @Test
    fun `isReservationAvailable- 현재 시간이 예매 기간 범위 내라면 true를 반환한다`() {
        // given
        val now = LocalDateTime.now()
        val round =
            PerformanceRound.create(
                roundStartTime = now.plusDays(2),
                reservationStartTime = now.minusHours(1),
                reservationEndTime = now.plusHours(1),
            )

        // when
        val result = round.isReservationAvailable

        // then
        assertTrue(result)
    }

    @Test
    fun `isReservationAvailable- 현재 시간이 예매 종료 시간을 지났다면 false를 반환한다`() {
        // given
        val now = LocalDateTime.now()
        val round =
            PerformanceRound.create(
                roundStartTime = now.plusDays(2),
                reservationStartTime = now.minusDays(1),
                // 이미 종료됨
                reservationEndTime = now.minusHours(1),
            )

        // when
        val result = round.isReservationAvailable

        // then
        assertFalse(result)
    }

    @Test
    fun `isReservationAvailable- 현재 시간이 예매 시작 시간 이전이라면 false를 반환한다`() {
        // given
        val now = LocalDateTime.now()
        val round =
            PerformanceRound.create(
                roundStartTime = now.plusDays(2),
                // 아직 예메 시작시간이 아님
                reservationStartTime = now.plusHours(1),
                reservationEndTime = now.plusDays(1),
            )

        // when
        val result = round.isReservationAvailable

        // then
        assertFalse(result)
    }
}
