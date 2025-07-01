package org.example.kufar.callback

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.PollAnswer

fun pollCallBack(pollAnswer: PollAnswer?, bot: TelegramBot) {
    if (pollAnswer != null) {
        pollAnswer.user().id()
        val chatId = pollAnswer.user().id() //maybe replace with CHAT_ID
        val pollId = pollAnswer.pollId()
        val options = pollAnswer.optionIds()

        println("chatid: $chatId")
        println("poll: $pollId")
        println("options:")
        println(options.toList().toString())
    }
}