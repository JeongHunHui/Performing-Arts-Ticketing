import http from 'k6/http';
import { Trend, Counter } from "k6/metrics";
import { sleep, check, fail } from 'k6';
import { uuidv4, randomItem } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";

const performanceDetailsDurations = new Trend("performance_details_duration");
const seatAreasDurations = new Trend("seat_areas_duration");
const ticketStatusDurations = new Trend("ticket_status_duration");
const tempReserveDurations = new Trend("temp_reserve_duration");
const discountListDurations = new Trend("discount_list_duration");
const paymentStartDurations = new Trend("payment_start_duration");
const paymentConfirmDurations = new Trend("payment_confirm_duration");

const totalSeatScale = new Counter("total_seat_scale");
const reservedTicketCounts = new Counter("reserved_ticket_counts");
const paidTicketCounts = new Counter("paid_ticket_counts");
const userReservationCount = new Counter("user_reservation_counts");

const baseUrl = __ENV.BASE_URL || "http://localhost:8080/api/v1";
const sleepDuration = Number(__ENV.SLEEP_DURATION) || 1;
const activateLog = Boolean(__ENV.ACTIVATE_LOG) || false;
const performanceId = __ENV.PERFORMANCE_ID;
const roundId = __ENV.ROUND_ID;
const maxUserCount = Number(__ENV.MAX_USER);

/** 확률에 따라 예약할 티켓 수 결정 (1:40%, 2:30%, 3:20%, 4:10%)
 *  maxReservationCount가 더 작으면 maxReservationCount를 반환 */
function chooseTicketCount(maxReservationCount) {
    let rnd = Math.random();
    if (rnd < 0.4) rnd = 1;
    else if (rnd < 0.7) rnd = 2;
    else if (rnd < 0.9) rnd = 3;
    else rnd = 4;
    return Math.min(maxReservationCount, rnd);
}

/** 0 이상, max 이하의 정수를 반환 */
function getRandomInt(max) {
    return Math.floor(Math.random() * max);
}

/** 배열에서 n개의 요소를 랜덤하게 선택하여 반환 */
function getRandomElements(arr, n) {
    const len = arr.length;
    if (n >= len) {
        return arr;
    }
    const indices = new Set();
    while (indices.size < n) {
        indices.add(getRandomInt(len));
    }
    return Array.from(indices).map(index => arr[index]);
}

/** API 호출 결과를 검사하고 실패 시 에러 출력 */
function checkOrFail(res, message) {
    if (!check(res, {[`${message} 성공`]: (r) => r.status === 200})) {
        const data = res.json();
        if (data.code && data.message) {
            console.error(`[${data.code}] ${data.message}`);
        } else {
            console.error(res);
        }
        fail(`${message} 실패`);
    }
}

function requestPerformanceDetails(performanceId) {
    const performanceDetailsRes = http.get(`${baseUrl}/performance/${performanceId}`);
    performanceDetailsDurations.add(performanceDetailsRes.timings.duration);
    return performanceDetailsRes;
}

function requestSeatAreas(performanceId) {
    const seatAreasRes = http.get(`${baseUrl}/performance/${performanceId}/seat-areas`);
    seatAreasDurations.add(seatAreasRes.timings.duration);
    return seatAreasRes;
}

function requestTicketStatus(roundId, areaId) {
    const ticketStatusRes = http.get(`${baseUrl}/reservation?roundId=${roundId}&areaId=${areaId}`);
    ticketStatusDurations.add(ticketStatusRes.timings.duration);
    return ticketStatusRes;
}

function requestTempReserve(performanceId, userId, ticketIds) {
    const tempReserveRes = http.patch(
        `${baseUrl}/reservation/temp`,
        JSON.stringify({
            performanceId: performanceId,
            userId: userId,
            ticketIds: ticketIds
        }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    tempReserveDurations.add(tempReserveRes.timings.duration);
    return tempReserveRes;
}

function requestDiscountList(selectedTickets) {
    const discountListRes = http.post(
        `${baseUrl}/seat-grade/find-applicable-discounts`,
        JSON.stringify({ seatGradeIds: [...new Set(selectedTickets.map(t => t.seatGradeId))] }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    discountListDurations.add(discountListRes.timings.duration);
    return discountListRes;
}

function requestPaymentStart(reservationId, paymentItems, paymentMethod, userId) {
    const paymentStartRes = http.patch(
        `${baseUrl}/payment/start`,
        JSON.stringify({
            reservationId: reservationId,
            paymentItems: paymentItems,
            paymentMethod: paymentMethod,
            userId: userId,
        }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    paymentStartDurations.add(paymentStartRes.timings.duration);
    return paymentStartRes;
}

function requestPaymentConfirm(paymentId, userId, reservationId) {
    const paymentConfirmRes = http.patch(
        `${baseUrl}/payment/confirm`,
        JSON.stringify({
            paymentId: paymentId,
            userId: userId,
            reservationId: reservationId,
        }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    paymentConfirmDurations.add(paymentConfirmRes.timings.duration);
    return paymentConfirmRes;
}

function info(message) {
    if (activateLog) console.info(message);
}

function error(message) {
    if (activateLog) console.error(message);
}

export let options = {
    scenarios: {
        ticketing: {
            executor: 'shared-iterations',
            iterations: maxUserCount,
            vus: maxUserCount,
        },
    },
    thresholds: {
        'performance_details_duration': ['p(95)<300'],
        'seat_areas_duration': ['p(95)<300'],
        'ticket_status_duration': ['p(95)<300'],
        'temp_reserve_duration': ['p(95)<300'],
    },
};

export function setup() {
    console.info(`---------- 테스트 시작 ----------`);
    console.info(`🚀 시작 시간: ${new Date().toLocaleString('ko-KR', {})}`);
    const seatAreasRes = requestSeatAreas(performanceId);
    const seatAreas = seatAreasRes.json().seatAreas;
    totalSeatScale.add(seatAreas.reduce((acc, curr) => acc + curr.positions.length, 0));
    const userIds = Array.from({ length: Math.floor(maxUserCount * 0.8) }, () => uuidv4());
    return { userIds: userIds };
}

/** 실행 명령어
 *  - 로컬: k6 run --out influxdb=http://localhost:8086/k6db --env MAX_USER=100 ReservationLoadTest.js
 *  - 원격: k6 run --out influxdb={influxdb-url} --env BASE_URL=${base-url} ReservationLoadTest.js
 */
export default function (data) {
    // ---------- 0. 예매 페이지 진입 ----------
    sleep(Math.random() * sleepDuration * 2);
    // 0-1. 공연 및 회차 선택
    const currentPerformanceId = performanceId;
    const currentRoundId = roundId;
    const currentUserId = randomItem(data.userIds);

    // 0-2. 공연 정보 조회
    const performanceDetailsRes = requestPerformanceDetails(currentPerformanceId);
    checkOrFail(performanceDetailsRes, '공연 정보 조회');
    const maxReservationCount = performanceDetailsRes.json().maxReservationCount;
    const ticketCount = chooseTicketCount(maxReservationCount);

    // 0-3. 좌석 영역 조회
    const seatAreasRes = requestSeatAreas(currentPerformanceId);
    checkOrFail(seatAreasRes, '좌석 영역 조회');
    const seatAreas = seatAreasRes.json().seatAreas;

    // ---------- 1. 좌석 선택 단계 ----------
    let reservationId = null;
    let selectedTickets = [];

    // 임시 예매를 성공하거나, 모든 좌석 영역이 매진될 때까지 좌석 선택 단계를 반복
    while (!reservationId && seatAreas.length > 0) {
        // 1-1. 좌석 영역 선택
        // 좌석 영역 선택 Think Time
        sleep(sleepDuration * 0.5 + Math.random() * sleepDuration * 0.5);
        const areaIndex = getRandomInt(seatAreas.length);
        const area = seatAreas.splice(areaIndex, 1)[0];
        const posMapping = {};
        for (let pos of area.positions) {
            posMapping[pos.id] = pos.seatGradeId;
        }

        // 임시 예매를 성공하거나, 해당 영역이 매진될 때까지 반복
        while (true) {
            // 1-2. 티켓 예매 여부 조회
            const ticketStatusRes = requestTicketStatus(currentRoundId, area.id);
            checkOrFail(ticketStatusRes, '티켓 예매 여부');
            const availableTickets = ticketStatusRes.json().tickets.filter(ticket => ticket.canReserve);
            if (availableTickets.length === 0) break; // 1-1부터 재시도 (다른 영역 선택)

            // 1-3. 티켓 선택
            // 티켓 선택 Think Time
            sleep(sleepDuration + Math.random() * sleepDuration * 0.5);
            const ticketsToReserve = getRandomElements(availableTickets, ticketCount);
            selectedTickets = ticketsToReserve.map(ticket => ({
                ticketId: ticket.id,
                seatGradeId: posMapping[ticket.seatPositionId]
            }));

            // 1-4. 임시 예매 API 호출
            const ticketIds = selectedTickets.map(t => t.ticketId);
            const tempReserveRes = requestTempReserve(currentPerformanceId, currentUserId, ticketIds);
            if (tempReserveRes.status === 200) {
                const data = tempReserveRes.json();
                reservationId = data.reservationId;
                reservedTicketCounts.add(ticketIds.length);
                info(`임시 예매 성공: ${data.reservationId}\n예매한 티켓 목록: ${ticketIds.join(', ')}`);
                break; // 좌석 선택 단계 종료
            } else {
                const responseData = tempReserveRes.json()
                if (responseData.code === 'RE001') {
                    error(`임시 예매 실패: [${tempReserveRes.json().code}] ${tempReserveRes.json().message}`);
                    break; // 예매 가능 수량을 초과하는 경우 종료
                }
                if (responseData.code !== 'RE004') {
                    fail(`임시 예매 실패: [${tempReserveRes.json().code}] ${tempReserveRes.json().message}`);
                }
            }
        }
    }

    if (!reservationId) return;

    // ---------- 2. 할인 선택 단계 ----------
    // 2-1. 할인 목록 API 호출
    const discountListRes = requestDiscountList(selectedTickets);
    checkOrFail(discountListRes, '할인 목록 조회');

    // 2-2. 할인 적용 및 결제 페이지 이동
    // 할인 적용 Think Time
    sleep(sleepDuration + Math.random() * sleepDuration * 0.5);
    const paymentItems = Object.values(
        selectedTickets.reduce((acc, ticket) => {
            const key = ticket.seatGradeId;
            if (acc[key]) {
                acc[key].reservationCount += 1;
            } else {
                acc[key] = {
                    seatGradeId: key,
                    reservationCount: 1,
                    discountId: null,
                };
            }
            return acc;
        }, {})
    );

    // ---------- 3. 결제 단계 ----------
    // 3-1. 결제 수단 선택
    // 결제 수단 선택 Think Time
    sleep(sleepDuration * 0.5 + Math.random() * sleepDuration * 0.5);
    const currentPaymentMethod = 'CREDIT_CARD';

    // 3-2. 결제 시작 API 호출
    const paymentStartRes = requestPaymentStart(reservationId, paymentItems, currentPaymentMethod, currentUserId);
    checkOrFail(paymentStartRes, '결제 시작');
    const paymentId = paymentStartRes.json().paymentId;

    // 3-3. 결제 진행
    // 사용자가 결제 페이지에서 결제를 진행
    // 결제 진행 Think Time
    sleep(sleepDuration * 2 + Math.random());

    // 3-4. 결제 승인 API 호출
    const paymentConfirmRes = requestPaymentConfirm(paymentId, currentUserId, reservationId);
    if (paymentConfirmRes.json().code === 'RE001') {
        error(`결제 승인 실패: [${paymentConfirmRes.json().code}] ${paymentConfirmRes.json().message}`);
        return; // 예매 가능 수량을 초과하는 경우 종료
    }
    checkOrFail(paymentConfirmRes, '결제 승인');
    info(`사용자 ${currentUserId}가 ${selectedTickets.length}개의 티켓을 결제하였습니다!`);
    userReservationCount.add(selectedTickets.length, { userId: currentUserId });
    paidTicketCounts.add(selectedTickets.length);
}

export function handleSummary(data) {
    let totalSeats = data.metrics.total_seat_scale.values.count;
    let reservedSeats = data.metrics.reserved_ticket_counts.values.count;
    let paidSeats = data.metrics.paid_ticket_counts.values.count;

    console.info(`---------- 테스트 결과 ----------`);
    console.info(`🏁 종료 시간: ${new Date().toLocaleString('ko-KR', {})}`);
    console.info(`🎟 총 티켓 수: ${totalSeats}`);
    console.info(`📌 예약된 티켓 수: ${reservedSeats}`);
    console.info(`💳 결제된 티켓 수: ${paidSeats}`);

    // 조건 검증 및 추가 로그 출력
    if (reservedSeats > totalSeats) console.error("❌ 예약된 티켓 수가 전체 티켓 수를 초과했습니다!");
    if (paidSeats > totalSeats) console.error("❌ 결제된 티켓 수가 전체 티켓 수를 초과했습니다!");

    const percentile = 95;
    const latency = 300;
    const performanceDetailsDuration = Number(data.metrics.performance_details_duration.values[`p(${percentile})`].toFixed(2));
    const seatAreasDuration = Number(data.metrics.seat_areas_duration.values[`p(${percentile})`].toFixed(2));
    const ticketStatusDuration = Number(data.metrics.ticket_status_duration.values[`p(${percentile})`].toFixed(2));
    const tempReserveDuration = Number(data.metrics.temp_reserve_duration.values[`p(${percentile})`].toFixed(2));

    console.info('📊 API 응답 시간 분석');
    console.info(`- ${performanceDetailsDuration >= latency ? '❌' : '✅'} 공연 상세 p${percentile}: ${performanceDetailsDuration}ms`);
    console.info(`- ${seatAreasDuration >= latency ? '❌' : '✅'} 좌석 영역 p${percentile}: ${seatAreasDuration}ms`);
    console.info(`- ${ticketStatusDuration >= latency ? '❌' : '✅'} 티켓 상태 p${percentile}: ${ticketStatusDuration}ms`);
    console.info(`- ${tempReserveDuration >= latency ? '❌' : '✅'} 임시 예매 p${percentile}: ${tempReserveDuration}ms`);
    console.info(`------------------------------`);
}
