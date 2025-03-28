package com.hunhui.ticketworld.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.annotation.Schedules
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BatchJobScheduler(
    private val jobLauncher: JobLauncher,
    private val updateReservationStatisticsJob: Job,
    private val cachePopularPerformanceJob: Job,
) {
    /**
     * 매일 00:00 and 10:30 ~ 23:30 까지 1시간 간격으로 실행 (ex: 00:00, 10:30, 11:30, ... 23:30)
     * updateReservationStatistics는 previousStandardTime부터 현재 standardTime까지 공연별 예매 티켓 수를 집계
     * cachePopularPerformance는 0시 부터 standardTime까지 공연 별 예매율을 계산하고, 이를 바탕으로 TOP 50 공연을 Redis에 캐싱
     */
    @Schedules(
        Scheduled(cron = "0 01 10-23 * * ?"),
        Scheduled(cron = "0 0 0 * * ?"),
    )
    fun runDailyPopularPerformanceJobs() {
        // 현재 실행 시각을 standardTime으로 사용 (초, 나노초는 0으로 설정)
        val standardTime = LocalDateTime.now().withSecond(0).withNano(0)
        val previousStandardTime = if (standardTime.minute == 30) standardTime.minusHours(1) else standardTime.minusMinutes(30)
        val startTime = previousStandardTime.withHour(0).withMinute(0)
        jobLauncher.run(
            updateReservationStatisticsJob,
            JobParametersBuilder()
                .addString("previousStandardTime", previousStandardTime.toString())
                .addString("standardTime", standardTime.toString())
                .toJobParameters(),
        )
        jobLauncher.run(
            cachePopularPerformanceJob,
            JobParametersBuilder()
                .addString("startTime", startTime.toString())
                .addString("standardTime", standardTime.toString())
                .toJobParameters(),
        )
    }
}
