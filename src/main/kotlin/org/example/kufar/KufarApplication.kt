package org.example.kufar

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.Gson
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

var TELEGRAM_TOKEN = ""
var CHAT_ID = 0L
var KUFAR_TOKEN = ""
var timer: PeriodicTimer? = null
val LOGGER = LoggerFactory.getLogger("Kufar")

@SpringBootApplication
class KufarApplication

fun main(args: Array<String>) {
    runApplication<KufarApplication>(*args)

    val properties = Properties()
    val inputStream = FileInputStream("service.properties")
    properties.load(inputStream)

    TELEGRAM_TOKEN = properties["telegram.bot.token"].toString()
    CHAT_ID = (properties["chat.id"] as String).toLong()
    KUFAR_TOKEN = properties["kufar.token"].toString()

    val bot = TelegramBot(TELEGRAM_TOKEN)
    timer = PeriodicTimer(10.minutes, bot)

    thread {
        timer?.start()
    }

    LOGGER.info("start service")

    bot.setUpdatesListener { updates ->
        updates.forEach { update ->
            val message = update.message()
            if (message != null) {
                val chatId = message.chat().id()

                val text = message.text()
                if (text == "/start") {
                    timer?.start()
                    val response = SendMessage(chatId, "Hello! I'm Kufar search bot\n Start scheduler")
                    bot.execute(response)

                    LOGGER.info("start schedule bot")
                } else if (text == "/kufar") {
                    getKufarData(chatId, bot, true)
                    LOGGER.info("/kufar")
                } else if (text == "/stop") {
                    timer?.stop()
                    val response = SendMessage(chatId, "Stop scheduler")
                    bot.execute(response)

                    LOGGER.info("stop schedule")
                } else if (text.startsWith("/timer", ignoreCase = true)) {
                    val parts = text.split(" ")

                    if (parts.size == 2) {
                        try {
                            val num1 = parts[1].toInt()

                            timer?.stop()
                            timer = PeriodicTimer(num1.minutes, bot)
                            timer?.start()

                            bot.execute(SendMessage(chatId, "Schedule set to $num1 minutes"))
                        } catch (e: NumberFormatException) {
                            bot.execute(SendMessage(chatId, "Please enter a number"))
                        }
                    } else {
                        timer?.stop()
                        timer = PeriodicTimer(10.minutes, bot)
                        timer?.start()

                        bot.execute(SendMessage(chatId, "Schedule set to 10 minutes"))
                    }

                } else {
                    val response = SendMessage(chatId, "Sorry, I don't understand that command.")
                    bot.execute(response)
                }
            }
        }

        UpdatesListener.CONFIRMED_UPDATES_ALL
    }

    Thread.currentThread().join()
}

fun getKufarData(chatId: Long, bot: TelegramBot, force: Boolean = false) {
    try {
        val url = URL("https://api.kufar.by/saved-search/v1/accounts/2008074/searches?limit=100")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        connection.setRequestProperty(
            "Authorization",
            "Bearer $KUFAR_TOKEN"
        )

        val responseCode = connection.responseCode
        LOGGER.info(connection.responseMessage)

        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputReader = BufferedReader(InputStreamReader(connection.inputStream))
            val jsonString = inputReader.use { it.readText() }
            val gson = Gson()
            val person = gson.fromJson(jsonString, Person::class.java)
            LOGGER.info(person.toString())

            val new = person.items[0].counters.new
            val new2 = person.items[1].counters.new

            if ((new != 0 || new2 != 0) || force) {
                val message = "New under 630 rub.: $new\n" +
                        "[link](https://re.kufar.by/l/vitebsk/snyat/kvartiru-dolgosrochno?cur=BYR&gtsy=country-belarus~province-vitebskaja_oblast~area-vitebsk~locality-vitebsk&prc=r%3A0%2C63000&prn=1000&r_pageType=saved_search&size=30&sort=lst.d)\n\n" +
                        "New total: $new2\n" +
                        "[total link](https://re.kufar.by/listings?ar=18&cat=1010&cur=USD&gtsy=country-belarus~province-vitebskaja_oblast~area-vitebsk~locality-vitebsk&prn=1000&rgn=6&rnt=1&size=30&sort=lst.d&typ=let&r_pageType=saved_search)"

                val response = SendMessage(chatId, message).parseMode(ParseMode.Markdown)
                bot.execute(response)
                LOGGER.info("Send: $message")
            } else {
                LOGGER.info("Nothing to send")
            }
        } else {
            val response = SendMessage(chatId, "Sorry, I couldn't retrieve the weather data.")
            bot.execute(response)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

data class Person(@JsonProperty("items") val items: List<Item>) {
    data class Item(@JsonProperty("id") val id: String, @JsonProperty("counters") val counters: Counters)

    data class Counters(@JsonProperty("new") val new: Int)
}

class PeriodicTimer(private val period: Duration, private val bot: TelegramBot) {
    private var isRunning = false
    private var thread: Thread? = null

    fun start() {
        if (!isRunning) {
            isRunning = true
            thread = Thread {
                while (isRunning) {
                    onTimerTick()
                    Thread.sleep(period.inWholeMilliseconds)
                }
            }
            thread?.start()
        }
    }

    fun stop() {
        isRunning = false
        thread?.interrupt()
    }

    private fun onTimerTick() {
        getKufarData(CHAT_ID, bot)
    }
}