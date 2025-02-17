package com.hunhui.ticketworld.application

import com.hunhui.ticketworld.application.dto.request.PaymentStartRequest
import com.hunhui.ticketworld.application.dto.response.PaymentStartResponse
import com.hunhui.ticketworld.application.repository.FakePaymentRepository
import com.hunhui.ticketworld.application.repository.FakePerformanceRepository
import com.hunhui.ticketworld.application.repository.FakeReservationRepository
import com.hunhui.ticketworld.application.repository.FakeSeatGradeRepository
import com.hunhui.ticketworld.common.error.AssertUtil.assertErrorCode
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.payment.Payment
import com.hunhui.ticketworld.domain.payment.PaymentMethod
import com.hunhui.ticketworld.domain.payment.PaymentStatus
import com.hunhui.ticketworld.domain.payment.exception.PaymentErrorCode
import com.hunhui.ticketworld.domain.performance.Performance
import com.hunhui.ticketworld.domain.performance.PerformanceGenre
import com.hunhui.ticketworld.domain.performance.PerformanceInfo
import com.hunhui.ticketworld.domain.performance.PerformanceRound
import com.hunhui.ticketworld.domain.performance.exception.PerformanceErrorCode.ROUND_NOT_AVAILABLE
import com.hunhui.ticketworld.domain.reservation.Reservation
import com.hunhui.ticketworld.domain.reservation.Ticket
import com.hunhui.ticketworld.domain.seatgrade.SeatGrade
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class PaymentServiceTest {
    /**
     * [정상 결제 시작 동작]
     * - Reservation에 기록된 좌석 등급별 예매 수량({seatGradeId → 2})과
     *   결제 요청(PaymentItemRequest)이 일치하며,
     * - 실제 SeatGrade 객체(VIP)가 정상 생성되고,
     * - 할인 id가 null인 경우 기본 할인(Discount.DEFAULT)이 적용되어
     *   단가 10,000원 × 2매 = 20,000원이 결제 총액에 반영된다.
     */
    @Test
    fun `정상 결제 시작 동작`() {
        // Arrange
        val userId = UUID.randomUUID()
        val seatGradeId = UUID.randomUUID()
        val performanceId = UUID.randomUUID()
        // 직접 전달할 값들
        val performanceRoundId = UUID.randomUUID()
        val seatAreaId = UUID.randomUUID()
        val seatPositionId = UUID.randomUUID()

        // 실제 도메인 객체인 Ticket과 Reservation을 이용하여 Reservation 생성 (2매 예매)
        val reservation =
            createReservation(
                userId = userId,
                performanceId = performanceId,
                performanceRoundId = performanceRoundId,
                seatAreaId = seatAreaId,
                seatPositionId = seatPositionId,
                seatGradeId = seatGradeId,
                ticketCount = 2,
            )
        val reservationRepository = FakeReservationRepository()
        reservationRepository.save(reservation)

        // 실제 도메인 객체인 SeatGrade 생성
        // 할인 목록이 비어 있으므로 discountId가 null이면 내부적으로 Discount.DEFAULT가 적용된다고 가정
        val seatGrade =
            SeatGrade(
                id = seatGradeId,
                performanceId = performanceId,
                name = "VIP",
                price = Money(10_000L),
                discounts = emptyList(),
            )
        val seatGradeRepository = FakeSeatGradeRepository()
        seatGradeRepository.saveAll(listOf(seatGrade))
        val paymentRepository = FakePaymentRepository()
        val performanceRepository = FakePerformanceRepository()

        val paymentService =
            PaymentService(
                performanceRepository = performanceRepository,
                reservationRepository = reservationRepository,
                seatGradeRepository = seatGradeRepository,
                paymentRepository = paymentRepository,
            )

        // PaymentStartRequest 작성: 예매된 좌석 등급에 대해 2매 결제 요청, 할인 id는 null
        val paymentItemRequest =
            PaymentStartRequest.PaymentItemRequest(
                seatGradeId = seatGradeId,
                reservationCount = 2,
                discountId = null,
            )
        val request =
            PaymentStartRequest(
                reservationId = reservation.id,
                paymentItems = listOf(paymentItemRequest),
                paymentMethod = PaymentMethod.CREDIT_CARD,
                userId = userId,
            )

        // Act
        val response: PaymentStartResponse = paymentService.startPayment(request)

        // Assert
        // 좌석 단가 10,000원 × 2매 = 20,000원이 결제 총액이어야 함
        assertNotNull(response.paymentId)
        assertEquals(20_000L, response.totalAmount)

        // PaymentRepository에 Payment가 정상 저장되었는지 검증
        val savedPayment = paymentRepository.getById(response.paymentId)
        assertNotNull(savedPayment)
        assertEquals(response.paymentId, savedPayment.id)
        assertEquals(1, savedPayment.items.size)
        val paymentItem = savedPayment.items.first()
        assertEquals("VIP", paymentItem.seatGradeName)
        assertEquals(2, paymentItem.reservationCount)
        assertEquals(10_000L * 2, paymentItem.originalPrice.amount)
        assertEquals(10_000L * 2, paymentItem.discountedPrice.amount)
    }

    /**
     * [좌석 등급 수량 불일치 시 예외 발생]
     * - Reservation에 기록된 티켓 수와 결제 요청의 수량이 불일치하면
     *   PaymentStartRequest.checkSeatGradeCountByReservation()에서 BusinessException(INVALID_START_PAYMENT_REQUEST)이 발생한다.
     */
    @Test
    fun `좌석 등급 수량 불일치 시 예외 발생`() {
        // Arrange
        val userId = UUID.randomUUID()
        val seatGradeId = UUID.randomUUID()
        val performanceId = UUID.randomUUID()
        val performanceRoundId = UUID.randomUUID()
        val seatAreaId = UUID.randomUUID()
        val seatPositionId = UUID.randomUUID()

        // Reservation 생성 시 1매만 예매된 상태
        val reservation =
            createReservation(
                userId = userId,
                performanceId = performanceId,
                performanceRoundId = performanceRoundId,
                seatAreaId = seatAreaId,
                seatPositionId = seatPositionId,
                seatGradeId = seatGradeId,
                ticketCount = 1,
            )
        val reservationRepository = FakeReservationRepository()
        reservationRepository.save(reservation)

        // 정상적인 SeatGrade 객체 생성
        val seatGrade =
            SeatGrade(
                id = seatGradeId,
                performanceId = performanceId,
                name = "VIP",
                price = Money(10_000L),
                discounts = emptyList(),
            )
        val seatGradeRepository = FakeSeatGradeRepository()
        seatGradeRepository.saveAll(listOf(seatGrade))
        val paymentRepository = FakePaymentRepository()
        val performanceRepository = FakePerformanceRepository()

        val paymentService =
            PaymentService(
                performanceRepository = performanceRepository,
                reservationRepository = reservationRepository,
                seatGradeRepository = seatGradeRepository,
                paymentRepository = paymentRepository,
            )

        // PaymentStartRequest 작성: 2매 결제 요청하지만 Reservation에는 1매만 있음
        val paymentItemRequest =
            PaymentStartRequest.PaymentItemRequest(
                seatGradeId = seatGradeId,
                reservationCount = 2,
                discountId = null,
            )
        val request =
            PaymentStartRequest(
                reservationId = reservation.id,
                paymentItems = listOf(paymentItemRequest),
                paymentMethod = PaymentMethod.CREDIT_CARD,
                userId = userId,
            )

        // Act & Assert
        assertErrorCode(PaymentErrorCode.INVALID_START_PAYMENT_REQUEST) {
            paymentService.startPayment(request)
        }
    }

    // 성공 케이스: 예매, 결제, Performance(예매 회차가 예매 가능) 모두 정상인 경우
    @Test
    fun `정상 결제 완료 동작`() {
        // Arrange
        val userId = UUID.randomUUID()
        val performanceId = UUID.randomUUID()
        val seatGradeId = UUID.randomUUID()
        // 필요한 ID들 직접 생성
        val performanceRoundId = UUID.randomUUID()
        val seatAreaId = UUID.randomUUID()
        val seatPositionId = UUID.randomUUID()

        // 현재 시각 기준 예매 가능한 회차 생성
        val now = LocalDateTime.now()
        val availableRound =
            PerformanceRound(
                id = performanceRoundId,
                roundStartTime = now.plusMinutes(30),
                reservationStartTime = now.minusMinutes(5),
                reservationEndTime = now.plusMinutes(5),
                isTicketCreated = true,
            )

        // Performance 생성 (예매 가능한 회차 포함)
        val performance =
            Performance(
                id = performanceId,
                info =
                    PerformanceInfo(
                        title = "Test Performance",
                        genre = PerformanceGenre.CONCERT,
                        posterUrl = "test.png",
                        location = "Test Hall",
                        description = "Test Description",
                    ),
                maxReservationCount = 100,
                rounds = listOf(availableRound),
            )

        // Reservation 생성 (예매 2매)
        val reservation =
            createReservation(
                userId = userId,
                performanceId = performanceId,
                performanceRoundId = performanceRoundId,
                seatAreaId = seatAreaId,
                seatPositionId = seatPositionId,
                seatGradeId = seatGradeId,
                ticketCount = 2,
            )

        // Payment 생성 (PENDING 상태)
        val payment = Payment.create(userId, PaymentMethod.CREDIT_CARD)
        // Payment는 Payment.create()로 생성 시 PENDING 상태로 설정됨

        // Fake Repository들에 객체 저장
        val reservationRepository = FakeReservationRepository().apply { save(reservation) }
        val paymentRepository = FakePaymentRepository().apply { save(payment) }
        val performanceRepository = FakePerformanceRepository().apply { this.save(performance) }

        // PaymentCompleteRequest 작성
        val request =
            com.hunhui.ticketworld.application.dto.request.PaymentCompleteRequest(
                paymentId = payment.id,
                userId = userId,
                reservationId = reservation.id,
            )

        val paymentService =
            PaymentService(
                performanceRepository = performanceRepository,
                reservationRepository = reservationRepository,
                seatGradeRepository = FakeSeatGradeRepository(),
                paymentRepository = paymentRepository,
            )

        // Act
        paymentService.completePayment(request)

        // Assert
        // PaymentRepository에 저장된 Payment의 상태가 COMPLETED여야 함
        val completedPayment = paymentRepository.getById(payment.id)
        assertEquals(PaymentStatus.COMPLETED, completedPayment.status)
        // Reservation은 확인(예매 확정 처리 후 Reservation.confirm()이 호출됨)
        val confirmedReservation = reservationRepository.getById(reservation.id)
        // 실제 도메인 객체에 따라 예매 확정 여부를 확인할 수 있는 프로퍼티가 있다면 검증(여기서는 confirm() 호출 후 동일 객체 반환으로 가정)
        assertEquals(confirmedReservation.paymentId, payment.id)
    }

    // 실패 케이스 1: Performance의 예매 가능 회차가 아님 (ROUND_NOT_AVAILABLE)
    @Test
    fun `예매 회차가 예매 가능하지 않으면 ROUND_NOT_AVAILABLE 예외 발생`() {
        // Arrange
        val userId = UUID.randomUUID()
        val performanceId = UUID.randomUUID()
        val seatGradeId = UUID.randomUUID()
        val performanceRoundId = UUID.randomUUID()
        val seatAreaId = UUID.randomUUID()
        val seatPositionId = UUID.randomUUID()

        // 현재 시각 기준으로 예매 기간이 지난 회차 생성
        val now = LocalDateTime.now()
        val notAvailableRound =
            PerformanceRound(
                id = performanceRoundId,
                roundStartTime = now.plusMinutes(30),
                reservationStartTime = now.minusMinutes(20),
                reservationEndTime = now.minusMinutes(10),
                isTicketCreated = true,
            )

        // Performance 생성 (예매 불가능한 회차)
        val performance =
            Performance(
                id = performanceId,
                info =
                    PerformanceInfo(
                        title = "Test Performance",
                        genre = PerformanceGenre.CONCERT,
                        posterUrl = "test.png",
                        location = "Test Hall",
                        description = "Test Description",
                    ),
                maxReservationCount = 100,
                rounds = listOf(notAvailableRound),
            )

        // Reservation 생성 (예매 2매)
        val reservation =
            createReservation(
                userId = userId,
                performanceId = performanceId,
                performanceRoundId = performanceRoundId,
                seatAreaId = seatAreaId,
                seatPositionId = seatPositionId,
                seatGradeId = seatGradeId,
                ticketCount = 2,
            )

        // Payment 생성 (PENDING 상태)
        val payment = Payment.create(userId, PaymentMethod.CREDIT_CARD)

        // Fake Repository들에 객체 저장
        val reservationRepository = FakeReservationRepository().apply { save(reservation) }
        val paymentRepository = FakePaymentRepository().apply { save(payment) }
        val performanceRepository = FakePerformanceRepository().apply { this.save(performance) }

        val request =
            com.hunhui.ticketworld.application.dto.request.PaymentCompleteRequest(
                paymentId = payment.id,
                userId = userId,
                reservationId = reservation.id,
            )

        val paymentService =
            PaymentService(
                performanceRepository = performanceRepository,
                reservationRepository = reservationRepository,
                seatGradeRepository = FakeSeatGradeRepository(),
                paymentRepository = paymentRepository,
            )

        // Act & Assert
        assertErrorCode(ROUND_NOT_AVAILABLE) {
            paymentService.completePayment(request)
        }
    }

    /**
     * 실제 Ticket 객체를 직접 생성하여 Reservation.createTempReservation()을 호출한다.
     * 전달받은 performanceRoundId, seatAreaId, seatPositionId, seatGradeId를 티켓 생성에 사용한다.
     */
    private fun createReservation(
        userId: UUID,
        performanceId: UUID,
        performanceRoundId: UUID,
        seatAreaId: UUID,
        seatPositionId: UUID,
        seatGradeId: UUID,
        ticketCount: Int,
    ): Reservation {
        val tickets =
            (1..ticketCount).map {
                Ticket.create(
                    performanceRoundId = performanceRoundId,
                    seatAreaId = seatAreaId,
                    seatPositionId = seatPositionId,
                    seatGradeId = seatGradeId,
                )
            }
        return Reservation.createTempReservation(tickets, userId, performanceId)
    }
}
