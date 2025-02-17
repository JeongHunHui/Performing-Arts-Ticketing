#!/bin/bash
# 이 스크립트는 현재 시간의 초가 0초가 될 때까지 대기한 후 첫 번째 k6 명령을 실행합니다.
# 그리고 2분 후(120초 후) 또 다시 0초에 맞춰 두 번째 k6 명령을 실행합니다.

# 현재 초(sec)를 가져와서 다음 0초까지 대기 시간 계산 (60초 주기)
function wait_for_zero_second() {
  current_sec=$(date +%S)
  # 10진수로 변환 (앞에 0이 있을 경우 문제 방지)
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

# 첫 번째 실행 전에 0초에 맞춰 대기
wait_for_zero_second

# 첫 번째 k6 명령 실행
echo "Starting first k6 run at $(date)"
k6 run \
  --out influxdb=http://localhost:8086/k6db \
  --env SLEEP_DURATION=2 \
  --env PERFORMANCE_ID=23da0b4d-1c87-4ab1-aefe-74344e3bf273 \
  --env ROUND_ID=0440ee0a-ad33-4605-a785-a2b7c46ccab5 \
  --env MAX_USER=500 \
  ReservationLoadTest.js

# 두 번째 실행: 1분(60초) 대기 후, 다시 0초에 맞춰 실행
echo "Waiting 1 minutes for the next run..."
sleep 60

wait_for_zero_second

echo "Starting second k6 run at $(date)"
k6 run \
  --out influxdb=http://localhost:8086/k6db \
  --env SLEEP_DURATION=2 \
  --env PERFORMANCE_ID=23da0b4d-1c87-4ab1-aefe-74344e3bf273 \
  --env ROUND_ID=e2c1ea31-bdff-4023-8e02-3123486f7709 \
  --env MAX_USER=500 \
  ReservationLoadTest.js
