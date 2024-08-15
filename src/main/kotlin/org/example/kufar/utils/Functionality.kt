package org.example.kufar.utils

import com.google.gson.Gson
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.*
import com.pengrad.telegrambot.request.SendMessage
import org.example.kufar.*
import org.example.kufar.model.Data
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration.Companion.minutes

val header = Pair("Authorization", "Bearer $KUFAR_TOKEN")

fun start(chatId: Long?, bot: TelegramBot) {
    timer?.start()
    bot.execute(SendMessage(chatId, "Hello! I'm Kufar search bot\n Start scheduler"))
    LOGGER.info("start schedule bot")
}

fun stop(chatId: Long?, bot: TelegramBot) {
    timer?.stop()
    bot.execute(SendMessage(chatId, "Stop scheduler"))
    LOGGER.info("stop schedule")
}

fun getKufarData(chatId: Long, bot: TelegramBot, force: Boolean = false) {
    try {
        LOGGER.info("get kufar data")

        val url = URL(SAVED_SEARCH_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty(header.first, header.second)

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
                        InlineKeyboardButton("link").url(UNDER_630_URL),
                        InlineKeyboardButton("total link").url(TOTAL_URL),
                        InlineKeyboardButton("view").callbackData("/view"),
                    )

//                    val menuKeyboard = ReplyKeyboardMarkup(KeyboardButton("/view"))

                    val message = "New under 630 rub.: $new\n" +
                            "New total: $new2"

                    val response = SendMessage(chatId, message)
                        .parseMode(ParseMode.Markdown)
                        .replyMarkup(inlineKeyboard)

                    bot.execute(response)
                    LOGGER.info("Send: $message")

                    viewAll(CHAT_ID, bot)
                    LOGGER.info("View all ads")

                    lastFirstValue = 0
                    lastSecondValue = 0
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

fun viewAll(chatId: Long, bot: TelegramBot) {
    val responseCode1 = simplePost(VIEW_FIRST_URL, header)
    val responseCode2 = simplePost(VIEW_SECOND_URL, header)

    if (!listOf(responseCode1, responseCode2).contains(HttpURLConnection.HTTP_NO_CONTENT)) {
        bot.execute(SendMessage(chatId, "view all it's fail"))
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

fun timer(chatId: Long?, bot: TelegramBot, text: String) {
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
        timer = PeriodicTimer(2.minutes, bot)
        timer?.start()

        bot.execute(SendMessage(chatId, "Schedule set to 5 minutes"))
    }
}

//delete
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