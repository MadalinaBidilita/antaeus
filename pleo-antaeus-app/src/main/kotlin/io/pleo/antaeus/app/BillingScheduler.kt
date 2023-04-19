package io.pleo.antaeus.app

import io.pleo.antaeus.core.services.BillingService
import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class BillingScheduler(
    private val billingService: BillingService
) {
    private val logger = KotlinLogging.logger {}
    private val scheduler = Executors.newScheduledThreadPool(1)

    private val task = Runnable {
        billingService.chargeAll(LocalDate.now())
    }

    fun stop() {
        scheduler.shutdown()
    }

    fun start() {
        // schedule for on next 2:00 a.m
        // knowing that the 3 European time zones overlap between 1:00 - 23:00
        logger.info("BillingScheduler started")
        val now = LocalDateTime.now()
        var nextRun = now.withHour(2).withMinute(0).withSecond(0)
        if (now > nextRun) nextRun = nextRun.plusDays(1)
        scheduler.scheduleWithFixedDelay(task, Duration.between(now, nextRun).toHours(), 24, TimeUnit.HOURS)
    }
}