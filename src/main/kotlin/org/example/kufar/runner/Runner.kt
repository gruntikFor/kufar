package org.example.kufar.runner

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import org.bson.Document
import org.example.kufar.LOGGER
import org.example.kufar.callback.buttonsCallBack
import org.example.kufar.callback.commandsCallBack
import org.example.kufar.callback.pollCallBack
import org.example.kufar.configuration.CHAT_ID
import org.example.kufar.configuration.DEFAULT_TIMER
import org.example.kufar.configuration.TELEGRAM_TOKEN
import org.example.kufar.configuration.periodicTimer
import org.example.kufar.db.getMongoCollection
import org.example.kufar.db.insertOrUpdateTimer
import org.example.kufar.timer.PeriodicTimer
import org.springframework.stereotype.Service
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.minutes

@Service
class Runner {

    fun run() {
        val bot = TelegramBot(TELEGRAM_TOKEN)
        periodicTimer = PeriodicTimer(DEFAULT_TIMER, bot)

        insertOrUpdateTimer(
            Document("chat_id", CHAT_ID.toString())
                .append("timer", DEFAULT_TIMER.inWholeMinutes.toString())
        )

        thread {
            periodicTimer?.start()
        }

        LOGGER.info("start service")

        bot.setUpdatesListener { updates ->
            updates.forEach { update ->
                buttonsCallBack(update.callbackQuery(), bot)
                commandsCallBack(update.message(), bot)
                pollCallBack(update.pollAnswer(), bot)
            }

            UpdatesListener.CONFIRMED_UPDATES_ALL
        }

        Thread.currentThread().join()
    }
}