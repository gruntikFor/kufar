package org.example.kufar.service

import com.google.gson.Gson
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import org.example.kufar.LOGGER
import org.example.kufar.configuration.FIRST_INIT
import org.example.kufar.configuration.ITEMS
import org.example.kufar.db.DBData
import org.example.kufar.db.getMongoCollection
import org.example.kufar.model.ConvertedData
import org.example.kufar.model.Data
import kotlin.collections.forEach

fun isFirstInit(): Boolean {
    if (!FIRST_INIT) return false

    FIRST_INIT = false
    return true
}

fun firstInitSelectedOptions(chatId: Long) {
    if (!isFirstInit()) return

    val mongoCollection = getMongoCollection()

    val filter = Filters.and(
        eq("chat_id", chatId.toString()),
        eq("show", true)
    )

    val find = mongoCollection
        ?.find(filter)
        ?.sort(Sorts.ascending("index"))

    find
        ?.toList()
        ?.let { it ->
            it.forEach { value ->
                val data = Gson().fromJson(value.toJson(), DBData::class.java)

                ITEMS.add(data)
            }
        }
}

fun filterList(fetchedItems: Map<String, Data.Item>): MutableList<ConvertedData> {
    val filteredItems = mutableListOf<ConvertedData>()

    ITEMS.map { item ->
        fetchedItems[item.product_id]?.let {
            val newItems = fetchedItems[item.product_id]?.counters?.new ?: 0

            filteredItems.add(
                ConvertedData(
                    item.product_id,
                    item.title,
                    item.query,
                    item.count,
                    newItems,
                )
            )
        }
    }

    LOGGER.info("filtered items:")
    filteredItems.forEach { println("${it.title}; old count: ${it.oldCount}, count: ${it.count}") }

    return filteredItems
}

fun needUpdate(convertedData: List<ConvertedData>): Boolean {
    return convertedData
        .filter { it.oldCount != it.count }
        .filter { it.count != 0 }
        .toList()
        .isNotEmpty()
}

fun updateNew(convertedData: List<ConvertedData>) {
    convertedData.forEach {
        getMongoCollection()?.updateOne(
            eq("product_id", it.product_id),
            Updates.set("count", it.count)
        )
    }
}

fun convertFetchedElements(data: Data): Map<String, Data.Item> {
    return data.items.associateBy { it.id }.also {
        LOGGER.info("fetched items:")
        it.forEach { println(it.value) }
    }
}

fun sendNewAds(
    bot: TelegramBot,
    chatId: Long,
    filteredItems: MutableList<ConvertedData>,
    inlineKeyboard: MutableList<InlineKeyboardButton>
) {
    bot.execute(
        SendMessage(chatId, buildMessage(filteredItems))
            .parseMode(ParseMode.Markdown)
            .replyMarkup(InlineKeyboardMarkup(*inlineKeyboard.toTypedArray()))
    )
}

