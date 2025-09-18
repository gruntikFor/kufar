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
import org.example.kufar.configuration.ITEMS
import org.example.kufar.configuration.FIRST_INIT
import org.example.kufar.configuration.SAVED_SEARCH_URL
import org.example.kufar.db.DBData
import org.example.kufar.db.getMongoCollection
import org.example.kufar.model.ConvertedData
import org.example.kufar.model.Data
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

fun getKufarData(chatId: Long, bot: TelegramBot, force: Boolean = false) {
    try {
        LOGGER.info("get kufar data")

        firstInitSelectedOptions(chatId)

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

            val fetchedItems = data.items.associateBy { it.id }
            LOGGER.info("__fetched items")

            fetchedItems.forEach { println(it.key + " " + it.value) }

            val filteredItems = filterList(fetchedItems)

            LOGGER.info("___filtered items")
            LOGGER.info(filteredItems.toString())

            filteredItems.forEach { println(it.title + " " + it.oldCount + " " + it.count) }

            val message = filteredItems.mapIndexed { index, it ->
                String.format("%d. %s: %s", index + 1, it.title, it.count)
            }.toList().joinToString("\n")

            if (force || needUpdate(filteredItems)) {
                val inlineKeyboards = filteredItems.mapIndexed { index, item ->
                    InlineKeyboardButton((index + 1).toString()).url(item.query)
                }.toMutableList()

                inlineKeyboards.add(InlineKeyboardButton("view").callbackData("/view"))

                val request = SendMessage(chatId, message)
                    .parseMode(ParseMode.Markdown)
                    .replyMarkup(InlineKeyboardMarkup(*inlineKeyboards.toTypedArray()))

                bot.execute(request)
                LOGGER.info("Send\n: $message")

                val newItemsCount = filteredItems.filter { it.count > 0 }.size

                if (newItemsCount > 0) {
                    filteredItems.forEach {
                        it.count = 0
                        it.oldCount = 0
                    }
                    viewAll(chatId, bot)
                    updateNew(filteredItems)
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

fun firstInitSelectedOptions(chatId: Long) {
    if (FIRST_INIT) return

    FIRST_INIT = true

    val mongoCollection = getMongoCollection()

    val filter = Filters.and(
        eq("chat_id", chatId.toString()),
        eq("show", true)
    )

    val find = mongoCollection?.find(filter)?.sort(Sorts.ascending("index"))

    find?.toList()?.let { it ->
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
