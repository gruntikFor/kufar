package org.example.kufar.callback

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.PollAnswer
import org.example.kufar.db.collections
import org.example.kufar.db.getMongoCollection

fun pollCallBack(pollAnswer: PollAnswer?, bot: TelegramBot) {
    if (pollAnswer != null) {
        pollAnswer.user().id()
        val chatId = pollAnswer.user().id() //maybe replace with CHAT_ID
//        val pollId = pollAnswer.pollId()
        val options = pollAnswer.optionIds()

//        println("chatid: $chatId")
//        println("poll: $pollId")
//        println("options:")
//        println(options.toList().toString())

        val mongoCollection = getMongoCollection()

        val filter = Filters.and(
            eq("chat_id", chatId.toString()),
            eq("index", options[0])
        )

        val find = mongoCollection?.find(filter)

        if (find?.first() != null) {
            val set = Updates.set("show", true)
            collections?.updateOne(filter, set)
        }
    }
}