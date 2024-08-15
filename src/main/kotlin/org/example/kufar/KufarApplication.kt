package org.example.kufar

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import org.example.kufar.utils.PeriodicTimer
import org.example.kufar.utils.buttonsCallBack
import org.example.kufar.utils.commandsCallBack
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.FileInputStream
import java.util.*
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.minutes

var TELEGRAM_TOKEN = ""
var CHAT_ID = 0L
var KUFAR_TOKEN = ""
var VIEW_FIRST_URL = ""
var VIEW_SECOND_URL = ""
var SAVED_SEARCH_URL = ""
var UNDER_630_URL = ""
var TOTAL_URL = ""

var timer: PeriodicTimer? = null

var lastFirstValue = 0
var lastSecondValue = 0

val LOGGER = LoggerFactory.getLogger("Kufar")

@SpringBootApplication
class KufarApplication

fun main(args: Array<String>) {
    runApplication<KufarApplication>(*args)

    val properties = Properties()
    val inputStream = FileInputStream("service.properties")
    properties.load(inputStream)

    TELEGRAM_TOKEN = properties["telegram.bot.token"].toString()
    CHAT_ID = (properties["chat.id"] as String).toLong()
    KUFAR_TOKEN = properties["kufar.token"].toString()
    VIEW_FIRST_URL = properties["kufar.view.first.url"].toString()
    VIEW_SECOND_URL = properties["kufar.view.second.url"].toString()
    SAVED_SEARCH_URL = properties["kufar.saved-search.url"].toString()
    UNDER_630_URL = properties["kufar.under.630.url"].toString()
    TOTAL_URL = properties["kufar.total.url"].toString()

    val bot = TelegramBot(TELEGRAM_TOKEN)
    timer = PeriodicTimer(2.minutes, bot)

    thread {
        timer?.start()
    }

    LOGGER.info("start service")

    bot.setUpdatesListener { updates ->
        updates.forEach { update ->
            buttonsCallBack(update.callbackQuery(), bot)
            commandsCallBack(update.message(), bot)
        }

        UpdatesListener.CONFIRMED_UPDATES_ALL
    }

    Thread.currentThread().join()
}