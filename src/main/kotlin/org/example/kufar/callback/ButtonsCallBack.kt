package org.example.kufar.callback

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import com.pengrad.telegrambot.request.SendMessage
import org.example.kufar.LOGGER
import org.example.kufar.configuration.CHAT_ID
import org.example.kufar.service.getKufarData
import org.example.kufar.service.start
import org.example.kufar.service.stop
import org.example.kufar.service.viewAll

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