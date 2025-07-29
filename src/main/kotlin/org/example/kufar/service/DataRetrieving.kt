package org.example.kufar.service

import com.google.gson.Gson
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import org.example.kufar.*
import org.example.kufar.configuration.*
import org.example.kufar.model.Data
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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
//                        InlineKeyboardButton("test").url(TEST_URL),
                        InlineKeyboardButton("link").url(UNDER_630_URL),
                        InlineKeyboardButton("total link").url(TOTAL_URL),
                        InlineKeyboardButton("view").callbackData("/view"),
                    )

                    val message ="""
                        New under 630 rub.: $new
                        New total: $new2
                    """.trimIndent()

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
            val response = SendMessage(chatId, "Sorry, I couldn't retrieve the Kufar data.")
            bot.execute(response)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}