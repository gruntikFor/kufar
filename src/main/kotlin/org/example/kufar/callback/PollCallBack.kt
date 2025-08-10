package org.example.kufar.callback

import com.google.gson.Gson
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.PollAnswer
import org.example.kufar.configuration.ITEMS
import org.example.kufar.db.DBData
import org.example.kufar.db.collections
import org.example.kufar.db.getMongoCollection
import org.example.kufar.service.getKufarData

fun pollCallBack(pollAnswer: PollAnswer?, bot: TelegramBot) {
    if (pollAnswer != null) {
        val chatId = pollAnswer.user().id()
        val options = pollAnswer.optionIds()
        val mongoCollection = getMongoCollection()

        val filter = Filters.and(
            eq("chat_id", chatId.toString()),
            Filters.`in`("index", *options)
        )

        val find = mongoCollection?.find(filter)?.sort(Sorts.ascending("index"))

        find?.toList()?.let { it ->
            ITEMS.clear()

            collections?.updateMany(filter, Updates.set("show", true))

            it.forEach { value ->
                val data = Gson().fromJson(value.toJson(), DBData::class.java)
                    .apply { show = true }

                ITEMS.add(data)
            }

            print("selected items size: " + ITEMS.size)
        }

        getKufarData(chatId, bot)
    }
}