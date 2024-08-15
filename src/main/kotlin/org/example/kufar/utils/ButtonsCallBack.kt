package org.example.kufar.utils

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import com.pengrad.telegrambot.request.SendMessage
import org.example.kufar.CHAT_ID
import org.example.kufar.LOGGER

fun buttonsCallBack(callbackQuery: CallbackQuery?, bot: TelegramBot) {
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
                viewAll(CHAT_ID, bot)

                bot.execute(SendMessage(CHAT_ID, "Ads watched"))
                LOGGER.info("Ads watched")
            }
        }

        bot.execute(AnswerCallbackQuery(callbackQuery.id()))
    }
}