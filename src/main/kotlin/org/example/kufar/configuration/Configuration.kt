package org.example.kufar.configuration

import org.example.kufar.timer.PeriodicTimer
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.util.*

const val DEFAULT_ITEM_URL="https://www.kufar.by/listings?"

var TELEGRAM_TOKEN = ""
var CHAT_ID = 0L
var KUFAR_TOKEN = ""
var VIEW_FIRST_URL = ""
var VIEW_SECOND_URL = ""
var SAVED_SEARCH_URL = ""
var UNDER_630_URL = ""
var TEST_URL = ""
var TOTAL_URL = ""

var periodicTimer: PeriodicTimer? = null

var lastFirstValue = 0
var lastSecondValue = 0

@Component
class Configuration {

    fun init() {
        val properties = Properties()
        val inputStream = FileInputStream("service.properties")
        properties.load(inputStream)

        TELEGRAM_TOKEN = properties["telegram.bot.token"].toString()
        CHAT_ID = (properties["chat.id"] as String).toLong()
        KUFAR_TOKEN = properties["kufar.token"].toString()
        VIEW_FIRST_URL = properties["kufar.view.first.url"].toString()
        VIEW_SECOND_URL = properties["kufar.view.second.url"].toString()
        SAVED_SEARCH_URL = properties["kufar.saved-search.url"].toString()
        UNDER_630_URL = properties["kufar.under.630.url"].toString()
        TOTAL_URL = properties["kufar.total.url"].toString()
    }
}