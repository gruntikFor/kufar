package org.example.kufar.service

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.InputPollOption
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPoll
import org.bson.Document
import org.example.kufar.LOGGER
import org.example.kufar.configuration.*
import org.example.kufar.db.insertOrUpdate
import org.example.kufar.timer.PeriodicTimer
import org.example.kufar.utils.simpleGet
import org.example.kufar.utils.simplePost
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
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
        } catch (_: NumberFormatException) {
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

fun favorite(chatId: Long, bot: TelegramBot) {
    val responseData = simpleGet(SAVED_SEARCH_URL, header)
    val names = responseData.items.map { it -> it.auto_names.ru }

    val documents = mutableListOf<Document>()

    for ((index, it) in responseData.items.withIndex()) {
        documents.add(
            Document("chat_id", chatId.toString())
                .append("product_id", it.id)
                .append("title", it.auto_names.ru)
                .append("query", it.query)
                .append("show", false)
                .append("index", index)
                .append("date", Date())
        )
    }

    insertOrUpdate(documents)

//    val find = find()
//    var lines = ""
//
//    for (line in find) {
//        println(line)
//        lines += line
//    }

    val toList = names.map { line -> InputPollOption(line) }.toList()

    val execute = bot.execute(
        SendPoll(
            chatId,
            "Выберите продукты для отслеживания",
            *toList.toTypedArray(),
        )
            .allowsMultipleAnswers(true)
            .isAnonymous(false)
    )

    if (!execute.isOk) {
        System.err.println("Ошибка при отправке опроса: ${execute.errorCode()} ${execute.description()}")
    }
}