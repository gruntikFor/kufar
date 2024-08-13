package org.example.kufar.utils

import com.pengrad.telegrambot.TelegramBot
import org.example.kufar.CHAT_ID
import org.example.kufar.getKufarData
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
        getKufarData(CHAT_ID, bot)
    }
}