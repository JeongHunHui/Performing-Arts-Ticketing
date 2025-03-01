#!/bin/bash
# 사용법: ./run_tests.sh <performanceId> <warmingUpRoundId> <mainRoundId> <mysqlContainerName> <mysqlUser> <mysqlPassword> <mysqlDatabase> <maxUser> <tempReserveLock> <selectReservationLock> <selectTicketsLock> <iterationCount>

if [ "$#" -ne 12 ]; then
  echo "Usage: $0 <performanceId> <warmingUpRoundId> <mainRoundId> <mysqlContainerName> <mysqlUser> <mysqlPassword> <mysqlDatabase> <maxUser> <tempReserveLock> <selectReservationLock> <selectTicketsLock> <iterationCount>"
  exit 1
fi

PERFORMANCE_ID=$1
WARMING_UP_ROUND_ID=$2
MAIN_ROUND_ID=$3
MYSQL_CONTAINER_NAME=$4
MYSQL_USER=$5
MYSQL_PASSWORD=$6
MYSQL_DATABASE=$7
MAX_USER=$8
TEMP_RESERVE_LOCK=$9
SELECT_RESERVATION_LOCK=${10}
SELECT_TICKETS_LOCK=${11}
ITERATION_COUNT=${12}

# 현재 초(sec)를 가져와서 다음 0초까지 대기 시간 계산 함수
function wait_for_zero_second() {
  current_sec=$(date +%S)
  current_sec=$((10#$current_sec))
  sleep_time=$((60 - current_sec))
  if [ $sleep_time -eq 60 ]; then
    sleep_time=0
  fi
  if [ $sleep_time -gt 0 ]; then
    echo "Waiting $sleep_time seconds for the next 0 second..."
    sleep $sleep_time
  fi
}

for ((i=1; i<=ITERATION_COUNT; i++)); do
  echo "===== Iteration $i 시작 ====="

  echo "Starting warming up at $(date)"
  k6 run \
    --env SLEEP_DURATION=0.5 \
    --env PERFORMANCE_ID=$PERFORMANCE_ID \
    --env ROUND_ID=$WARMING_UP_ROUND_ID \
    --env MAX_USER=100 \
    --env TEMP_RESERVE_LOCK=$TEMP_RESERVE_LOCK \
    --env SELECT_RESERVATION_LOCK=$SELECT_RESERVATION_LOCK \
    --env SELECT_TICKETS_LOCK=$SELECT_TICKETS_LOCK \
    ReservationLoadTest.js

  echo "Waiting 10 seconds for the next run..."
  sleep 10

  wait_for_zero_second

  echo "Starting ReservationLoadTest at $(date)"
  k6 run \
    --out influxdb=http://localhost:8086/k6db \
    --env SLEEP_DURATION=2 \
    --env PERFORMANCE_ID=$PERFORMANCE_ID \
    --env ROUND_ID=$MAIN_ROUND_ID \
    --env MAX_USER=$MAX_USER \
    --env TEMP_RESERVE_LOCK=$TEMP_RESERVE_LOCK \
    --env SELECT_RESERVATION_LOCK=$SELECT_RESERVATION_LOCK \
    --env SELECT_TICKETS_LOCK=$SELECT_TICKETS_LOCK \
    ReservationLoadTest.js

  echo "Executing cleanup SQL query..."
  docker exec -i "$MYSQL_CONTAINER_NAME" mysql -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" <<EOF
START TRANSACTION;
SET @performanceId = UUID_TO_BIN('$PERFORMANCE_ID');
DELETE FROM payment_item pi
WHERE pi.payment_id IN (
    SELECT r.payment_id FROM reservation r
    WHERE r.performance_id = @performanceId OR
          pi.payment_id IN (
              SELECT p.id FROM payment p
              WHERE p.status = 'PENDING'
          )
);
DELETE FROM payment p
WHERE p.id IN (
    SELECT r.payment_id FROM reservation r
    WHERE r.performance_id = @performanceId OR
          p.status = 'PENDING'
);
UPDATE ticket
SET is_paid = FALSE,
    reservation_id = NULL,
    expire_time = NOW()
WHERE reservation_id IN (
    SELECT r.id FROM reservation r
    WHERE performance_id = @performanceId
);
DELETE FROM reservation
WHERE performance_id = @performanceId;
COMMIT;
EOF

  echo "Restarting ticket-world-spring-1 container..."
  docker restart performing-arts-ticketing-ticket-world-spring-1

  echo "Restarting MySQL container ($MYSQL_CONTAINER_NAME)..."
  docker restart "$MYSQL_CONTAINER_NAME"

  echo "===== Iteration $i 완료 ====="

  if [ $i -ne "$ITERATION_COUNT" ]; then
    echo "Waiting 20 seconds before next iteration..."
    sleep 20
  fi
done

echo "모든 반복이 완료되었습니다."
