package org.example.kufar.callback

import com.google.gson.Gson
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.PollAnswer
import org.example.kufar.configuration.DEFAULT_ITEM_URL
import org.example.kufar.configuration.ITEMS
import org.example.kufar.db.DBData
import org.example.kufar.db.collections
import org.example.kufar.db.getMongoCollection
import org.example.kufar.model.Data

fun pollCallBack(pollAnswer: PollAnswer?, bot: TelegramBot) {
    if (pollAnswer != null) {
        pollAnswer.user().id()
        val chatId = pollAnswer.user().id() //maybe replace with CHAT_ID
        val options = pollAnswer.optionIds()
        options.toList().toTypedArray()

        val mongoCollection = getMongoCollection()

        val filter = Filters.and(
            eq("chat_id", chatId.toString()),
            Filters.`in`("index", *options)
        )

        val find = mongoCollection?.find(filter)?.sort(Sorts.ascending("index"))

        find?.toList()?.let { it ->
            ITEMS.clear()

            val set = Updates.set("show", true)
            collections?.updateMany(filter, set)

            it.forEach { value ->
                val data = Gson().fromJson(value.toJson(), DBData::class.java)
                    .apply { show = true }
                    .apply { query = DEFAULT_ITEM_URL + query }

                ITEMS.add(data)
            }

            print("selected items size: " + ITEMS.size)
        }
    }
}