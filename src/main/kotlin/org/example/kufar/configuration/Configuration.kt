package org.example.kufar.configuration

import org.example.kufar.db.DBData
import org.example.kufar.timer.PeriodicTimer
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.util.*

const val DEFAULT_ITEM_URL = "https://www.kufar.by/listings?"
const val DEFAULT_RENT_URL = "https://re.kufar.by/listings?"
const val DEFAULT_VIEW_API_URL = "https://api.kufar.by/saved-search/v1/accounts/{accountId}/searches/{productId}/views"

var TELEGRAM_TOKEN = ""
var CHAT_ID = 0L
var KUFAR_TOKEN = ""
var SAVED_SEARCH_URL = ""
var ITEMS = mutableListOf<DBData>()
var FIRST_INIT = false

var periodicTimer: PeriodicTimer? = null

@Component
class Configuration {

    fun init() {
        val properties = Properties()
        val inputStream = FileInputStream("service.properties")
        properties.load(inputStream)

        TELEGRAM_TOKEN = properties["telegram.bot.token"].toString()
        CHAT_ID = (properties["chat.id"] as String).toLong()
        KUFAR_TOKEN = properties["kufar.token"].toString()
        SAVED_SEARCH_URL = properties["kufar.saved-search.url"].toString()
    }
}