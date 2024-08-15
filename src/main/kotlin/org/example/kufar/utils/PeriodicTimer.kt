package org.example.kufar.utils

import com.pengrad.telegrambot.TelegramBot
import org.example.kufar.CHAT_ID
import org.example.kufar.LOGGER
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

class PeriodicTimer(private val period: Duration, private val bot: TelegramBot) {
    private var isRunning = false
    private var thread: Thread? = null

    fun start() {
        if (!isRunning) {
            isRunning = true
            thread = Thread {
                while (isRunning) {
                    onTimerTick()
                    Thread.sleep(period.inWholeMilliseconds)
                }
            }
            thread?.start()
        }
    }

    fun stop() {
        isRunning = false
        thread?.interrupt()
    }

    private fun onTimerTick() {
        val dateTimePlus3 = getCurrentDataTime()

        if (dateTimePlus3.hour in 7..23) {
            getKufarData(CHAT_ID, bot)
        } else {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            LOGGER.info("I'm sleeping: ${dateTimePlus3.format(formatter)}")
        }
    }

    private fun getCurrentDataTime(): ZonedDateTime {
        return ZonedDateTime.now(ZoneOffset.ofHours(3))
    }
}