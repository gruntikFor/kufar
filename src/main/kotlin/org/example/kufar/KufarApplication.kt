package org.example.kufar

import com.google.gson.Gson
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import com.pengrad.telegrambot.request.SendMessage
import org.example.kufar.model.Data
import org.example.kufar.utils.PeriodicTimer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.minutes

var TELEGRAM_TOKEN = ""
var CHAT_ID = 0L
var KUFAR_TOKEN = ""
var timer: PeriodicTimer? = null

var lastFirstValue = 0
var lastSecondValue = 0

val LOGGER = LoggerFactory.getLogger("Kufar")

@SpringBootApplication
class KufarApplication

fun wholeCallBack(callbackQuery: CallbackQuery?, bot: TelegramBot) {
    if (callbackQuery != null) {
        when (callbackQuery.data()) {
            "/start" -> {
                start(CHAT_ID, bot)
            }

            "/stop" -> {
                stop(CHAT_ID, bot)
            }

            "/kufar" -> {
                getKufarData(CHAT_ID, bot, true)
            }

            "/view" -> {
                view1(CHAT_ID, bot)
                view2(CHAT_ID, bot)

                bot.execute(SendMessage(CHAT_ID, "Ads watched"))
                LOGGER.info("Ads watched")
            }
        }

        bot.execute(AnswerCallbackQuery(callbackQuery.id()))
    }
}

fun main(args: Array<String>) {
    runApplication<KufarApplication>(*args)

    val properties = Properties()
    val inputStream = FileInputStream("service.properties")
    properties.load(inputStream)

    TELEGRAM_TOKEN = properties["telegram.bot.token"].toString()
    CHAT_ID = (properties["chat.id"] as String).toLong()
    KUFAR_TOKEN = properties["kufar.token"].toString()

    val bot = TelegramBot(TELEGRAM_TOKEN)
    timer = PeriodicTimer(5.minutes, bot)

    thread {
        timer?.start()
    }

    LOGGER.info("start service")

    bot.setUpdatesListener { updates ->
        updates.forEach { update ->
            val message = update.message()
            wholeCallBack(update.callbackQuery(), bot)

            if (message != null) {
                val chatId = message.chat().id() //maybe replace with CHAT_ID
                val text = message.text()

                if (text == "/start") {
                    start(chatId, bot)

                } else if (text == "/kufar") {
                    getKufarData(chatId, bot, true)

                } else if (text == "/stop") {
                    stop(chatId, bot)

                } else if (text.startsWith("/timer")) {
                    timer(chatId, bot, text)

                } else if (text == "/fast") {
                    timer(chatId, bot, "/timer 2")

                } else if (text == "/slow") {
                    timer(chatId, bot, "/timer 5")

                } else if (text == "/sleep") {
                    timer(chatId, bot, "/timer 240")

                } else if (text == "/test") {
                    test(chatId, bot)

                } else if (text == "/view1") {
                    view1(chatId, bot)

                } else if (text == "/view2") {
                    view2(chatId, bot)

                } else {
                    val response = SendMessage(chatId, "Sorry, I don't understand that command.")
                    bot.execute(response)
                }
            }
        }

        UpdatesListener.CONFIRMED_UPDATES_ALL
    }

    Thread.currentThread().join()
}

private fun start(chatId: Long?, bot: TelegramBot) {
    timer?.start()
    val response = SendMessage(chatId, "Hello! I'm Kufar search bot\n Start scheduler")
    bot.execute(response)

    LOGGER.info("start schedule bot")
}

private fun stop(chatId: Long?, bot: TelegramBot) {
    timer?.stop()
    val response = SendMessage(chatId, "Stop scheduler")
    bot.execute(response)

    LOGGER.info("stop schedule")
}

private fun timer(chatId: Long?, bot: TelegramBot, text: String) {
    val parts = text.split(" ")

    if (parts.size == 2) {
        try {
            val num1 = parts[1].toInt()

            timer?.stop()
            timer = PeriodicTimer(num1.minutes, bot)
            timer?.start()

            bot.execute(SendMessage(chatId, "Schedule set to $num1 minutes"))
        } catch (e: NumberFormatException) {
            bot.execute(SendMessage(chatId, "Please enter a number"))
        }
    } else {
        timer?.stop()
        timer = PeriodicTimer(5.minutes, bot)
        timer?.start()

        bot.execute(SendMessage(chatId, "Schedule set to 5 minutes"))
    }
}

fun getKufarData(chatId: Long, bot: TelegramBot, force: Boolean = false) {
    try {
        LOGGER.info("get kufar data")

        val url = URL("https://api.kufar.by/saved-search/v1/accounts/2008074/searches?limit=100")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        connection.setRequestProperty(
            "Authorization",
            "Bearer $KUFAR_TOKEN"
        )

        val responseCode = connection.responseCode
        LOGGER.info(connection.responseMessage)

        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputReader = BufferedReader(InputStreamReader(connection.inputStream))
            val jsonString = inputReader.use { it.readText() }
            val data = Gson().fromJson(jsonString, Data::class.java)
            LOGGER.info(data.toString())

            val new = data.items[0].counters.new
            val new2 = data.items[1].counters.new

            if ((lastFirstValue != new || lastSecondValue != new2) || force) {
                if ((new != 0 || new2 != 0) || force) {
                    lastFirstValue = new
                    lastSecondValue = new2

                    val inlineKeyboard = InlineKeyboardMarkup(
                        InlineKeyboardButton("link").url("https://re.kufar.by/l/vitebsk/snyat/kvartiru-dolgosrochno?cur=BYR&gtsy=country-belarus~province-vitebskaja_oblast~area-vitebsk~locality-vitebsk&prc=r%3A0%2C63000&prn=1000&r_pageType=saved_search&size=30&sort=lst.d"),
                        InlineKeyboardButton("total link").url("https://re.kufar.by/listings?ar=18&cat=1010&cur=USD&gtsy=country-belarus~province-vitebskaja_oblast~area-vitebsk~locality-vitebsk&prn=1000&rgn=6&rnt=1&size=30&sort=lst.d&typ=let&r_pageType=saved_search"),
                        InlineKeyboardButton("view").callbackData("/view")
                    )

                    val message = "New under 630 rub.: $new\n" +
                            "New total: $new2"

                    val response =
                        SendMessage(chatId, message).parseMode(ParseMode.Markdown).replyMarkup(inlineKeyboard)

                    bot.execute(response)
                    LOGGER.info("Send: $message")
                }
            } else {
                LOGGER.info("Nothing to send")
            }
        } else {
            val response = SendMessage(chatId, "Sorry, I couldn't retrieve the weather data.")
            bot.execute(response)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun test(chatId: Long, bot: TelegramBot) {
    val inlineKeyboard = InlineKeyboardMarkup(
        InlineKeyboardButton("start").callbackData("/start"),
        InlineKeyboardButton("kufar").callbackData("/kufar"),
        InlineKeyboardButton("stop").callbackData("/stop"),
        InlineKeyboardButton("view").callbackData("/view")
    )

    bot.execute(SendMessage(chatId, "select command").replyMarkup(inlineKeyboard))
}

fun view1(chatId: Long, bot: TelegramBot) {
    val url = URL("https://api.kufar.by/saved-search/v1/accounts/2008074/searches/2008074.20240801185141.946/views")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"

    connection.setRequestProperty(
        "Authorization",
        "Bearer $KUFAR_TOKEN"
    )

    if (connection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        bot.execute(SendMessage(chatId, "view 1 it's fail"))
    }
}

fun view2(chatId: Long, bot: TelegramBot) {
    val url = URL("https://api.kufar.by/saved-search/v1/accounts/2008074/searches/2008074.20240731111856.466/views")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"

    connection.setRequestProperty(
        "Authorization",
        "Bearer $KUFAR_TOKEN"
    )

    if (connection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        bot.execute(SendMessage(chatId, "view 2 it's fail"))
    }
}