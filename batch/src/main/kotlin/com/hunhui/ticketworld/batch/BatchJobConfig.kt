package com.hunhui.ticketworld.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

@Configuration
class BatchJobConfig(
    private val jobRepository: JobRepository,
    private val platformTransactionManager: PlatformTransactionManager,
    private val statisticsService: UpdateReservationStatisticsService,
    private val cacheService: CachePopularPerformanceService,
) {
    @Bean
    fun updateReservationStatisticsJob(): Job =
        JobBuilder("updateReservationStatisticsJob", jobRepository)
            .start(updateReservationStatisticsStep())
            .build()

    @Bean
    fun updateReservationStatisticsStep(): Step =
        StepBuilder("updateReservationStatisticsStep", jobRepository)
            .tasklet(
                { _, chunkContext ->
                    val jobParameters = chunkContext.stepContext.jobParameters
                    val previousStandardTime = LocalDateTime.parse(jobParameters["previousStandardTime"] as String)
                    val standardTime = LocalDateTime.parse(jobParameters["standardTime"] as String)
                    statisticsService.updateReservationStatistics(previousStandardTime, standardTime)
                    RepeatStatus.FINISHED
                },
                platformTransactionManager,
            ).build()

    @Bean
    fun cachePopularPerformanceJob(): Job =
        JobBuilder("cachePopularPerformanceJob", jobRepository)
            .start(cachePopularPerformanceStep())
            .build()

    @Bean
    fun cachePopularPerformanceStep(): Step =
        StepBuilder("cachePopularPerformanceStep", jobRepository)
            .tasklet(
                { _, chunkContext ->
                    val jobParameters = chunkContext.stepContext.jobParameters
                    val startTime = LocalDateTime.parse(jobParameters["startTime"] as String)
                    val standardTime = LocalDateTime.parse(jobParameters["standardTime"] as String)
                    cacheService.cachePopularPerformances(startTime, standardTime)
                    RepeatStatus.FINISHED
                },
                platformTransactionManager,
            ).build()
}
