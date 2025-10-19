package org.example.kufar.service

import com.pengrad.telegrambot.TelegramBot
import org.example.kufar.LOGGER
import org.example.kufar.configuration.SAVED_SEARCH_URL
import org.example.kufar.request.simpleGetWithCodeCheck

fun getKufarData(chatId: Long, bot: TelegramBot, force: Boolean = false) {
    try {
        LOGGER.info("get kufar data")
        firstInitSelectedOptions(chatId)

        val data = simpleGetWithCodeCheck(SAVED_SEARCH_URL, bot)

        data?.let { it ->
            val filteredItems = filterList(convertFetchedElements(it))

            if (force || needUpdate(filteredItems)) {
                sendNewAds(bot, chatId, filteredItems, buildInlineKeyboard(filteredItems))
                updateNew(filteredItems)
            } else {
                LOGGER.info("Nothing to send")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
