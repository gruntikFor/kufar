package org.example.kufar.callback

import com.google.gson.Gson
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.PollAnswer
import com.pengrad.telegrambot.model.Update
import org.example.kufar.configuration.DEFAULT_ITEM_URL
import org.example.kufar.configuration.ITEMS
import org.example.kufar.configuration.TEST_URL
import org.example.kufar.configuration.URLS
import org.example.kufar.db.DBData
import org.example.kufar.db.collections
import org.example.kufar.db.getMongoCollection

fun pollCallBack(pollAnswer: PollAnswer?, bot: TelegramBot) {
    if (pollAnswer != null) {
        pollAnswer.user().id()
        val chatId = pollAnswer.user().id() //maybe replace with CHAT_ID
//        val pollId = pollAnswer.pollId()
        val options = pollAnswer.optionIds()
        options.toList().toTypedArray()
//        println("chatid: $chatId")
//        println("poll: $pollId")
//        println("options:")
//        println(options.toList().toString())

        val mongoCollection = getMongoCollection()

        val filter = Filters.and(
            eq("chat_id", chatId.toString()),
            Filters.`in`("index", *options)
        )

        val find = mongoCollection?.find(filter)

        find?.toList()?.let { it ->
            ITEMS.clear()

            val set = Updates.set("show", true)
            collections?.updateMany(filter, set)

            it.forEach { value ->
                URLS.add(value["query"].toString()) //todo del
                ITEMS.add(Gson().fromJson(value.toJson(), DBData::class.java))
            }

            //todo del
            val query = find.first()?.get("query")
            TEST_URL = DEFAULT_ITEM_URL + query.toString()

            print("selected items size: " + ITEMS.size)

            //call banner
        }
    }
}