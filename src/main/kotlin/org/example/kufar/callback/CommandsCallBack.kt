package org.example.kufar.callback

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.SendMessage
import org.example.kufar.LOGGER
import org.example.kufar.configuration.CHAT_ID
import org.example.kufar.service.*

fun commandsCallBack(message: Message?, bot: TelegramBot) {
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
//            test(chatId, bot)

        } else if (text == "/view") {
            viewAll(chatId, bot)
        } else if (text == "/favorite") {
            favorite(CHAT_ID, bot)
            LOGGER.info("Favorite")
        } else {
            val response = SendMessage(chatId, "Sorry, I don't understand that command.")
            bot.execute(response)
        }
    }
}