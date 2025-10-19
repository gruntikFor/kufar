package org.example.kufar.service

import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import org.example.kufar.LOGGER
import org.example.kufar.model.ConvertedData

fun buildMessage(filteredItems: MutableList<ConvertedData>): String {
    val message = filteredItems.mapIndexed { index, it ->
        String.format("%d. %s: %s", index + 1, it.title, it.count)
    }.toList().joinToString("\n")

    LOGGER.info("Send:\n$message")

    return message
}

fun buildInlineKeyboard(filteredItems: MutableList<ConvertedData>): MutableList<InlineKeyboardButton> {
    val inlineKeyboards =
        filteredItems.mapIndexed { index, item ->
            InlineKeyboardButton((index + 1).toString()).url(item.query)
        }.toMutableList()

    inlineKeyboards.add(InlineKeyboardButton("view").callbackData("/view"))
    return inlineKeyboards
}