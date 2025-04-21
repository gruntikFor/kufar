package org.example.kufar.service

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import org.example.kufar.*
import org.example.kufar.configuration.KUFAR_TOKEN
import org.example.kufar.configuration.VIEW_FIRST_URL
import org.example.kufar.configuration.VIEW_SECOND_URL
import org.example.kufar.configuration.periodicTimer
import org.example.kufar.timer.PeriodicTimer
import org.example.kufar.utils.simplePost
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration.Companion.minutes

val header = Pair("Authorization", "Bearer $KUFAR_TOKEN")

fun start(chatId: Long?, bot: TelegramBot) {
    periodicTimer?.start()
    bot.execute(SendMessage(chatId, "Hello! I'm Kufar search bot\n Start scheduler"))
    LOGGER.info("start schedule bot")
}

fun stop(chatId: Long?, bot: TelegramBot) {
    periodicTimer?.stop()
    bot.execute(SendMessage(chatId, "Stop scheduler"))
    LOGGER.info("stop schedule")
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

            periodicTimer?.stop()
            periodicTimer = PeriodicTimer(num1.minutes, bot)
            periodicTimer?.start()

            bot.execute(SendMessage(chatId, "Schedule set to $num1 minutes"))
        } catch (e: NumberFormatException) {
            bot.execute(SendMessage(chatId, "Please enter a number"))
        }
    } else {
        periodicTimer?.stop()
        periodicTimer = PeriodicTimer(2.minutes, bot)
        periodicTimer?.start()

        bot.execute(SendMessage(chatId, "Schedule set to 5 minutes"))
    }
}

//delete
fun view1(chatId: Long, bot: TelegramBot) {
    val url = URL("https://api.kufar.by/saved-search/v1/accounts/2008074/searches/2008074.20240801185141.946/views")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty(header.first, header.second)

    if (connection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        bot.execute(SendMessage(chatId, "view 1 it's fail"))
    }
}

fun view2(chatId: Long, bot: TelegramBot) {
    val url = URL("https://api.kufar.by/saved-search/v1/accounts/2008074/searches/2008074.20240731111856.466/views")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty(header.first, header.second)

    if (connection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        bot.execute(SendMessage(chatId, "view 2 it's fail"))
    }
}