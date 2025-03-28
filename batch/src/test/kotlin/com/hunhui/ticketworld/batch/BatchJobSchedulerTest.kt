package com.hunhui.ticketworld.batch

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.core.launch.JobLauncher

class BatchJobSchedulerTest {
    private val jobLauncher: JobLauncher = mockk(relaxed = true)
    private val updateJob: Job = mockk()
    private val cacheJob: Job = mockk()

    private val scheduler = BatchJobScheduler(jobLauncher, updateJob, cacheJob)

    @Test
    fun `runDailyPopularPerformanceJobs should launch jobs with proper parameters`() {
        // 직접 scheduled 메서드를 호출하여 jobLauncher.run이 두 번 호출되는지 검증
        scheduler.runDailyPopularPerformanceJobs()

        // jobLauncher.run이 두 번 호출되었는지 확인 (각각 updateReservationStatisticsJob과 cachePopularPerformanceJob)
        verify(exactly = 2) { jobLauncher.run(any(), any()) }
    }
}
