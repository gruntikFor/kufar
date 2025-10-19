package org.example.kufar.request

import com.google.gson.Gson
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import org.example.kufar.LOGGER
import org.example.kufar.configuration.CHAT_ID
import org.example.kufar.model.Data
import org.example.kufar.service.header
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private const val POST = "POST"
private const val GET = "GET"

fun simplePost(url: String, pair: Pair<String, String> = header): Int {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = POST
    connection.setRequestProperty(pair.first, pair.second)

    return connection.responseCode
}

fun simpleGet(url: String, pair: Pair<String, String> = header): Data {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = GET
    connection.setRequestProperty(pair.first, pair.second)

    val inputReader = BufferedReader(InputStreamReader(connection.inputStream))
    val jsonString = inputReader.use { it.readText() }
    val data = Gson().fromJson(jsonString, Data::class.java)
    LOGGER.info(data.toString())

    return data
}

fun simpleGetWithCodeCheck(url: String, bot: TelegramBot): Data? {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = GET
    connection.setRequestProperty(header.first, header.second)

    val responseCode = connection.responseCode

    if (responseCode == HttpURLConnection.HTTP_OK) {
        val inputReader = BufferedReader(InputStreamReader(connection.inputStream))
        val json = inputReader.use { it.readText() }
        val data = Gson().fromJson(json, Data::class.java)
        LOGGER.info(data.toString())

        return data
    }

    bot.execute(
        SendMessage(CHAT_ID, "Sorry, I couldn't retrieve the Kufar data. Response code: $responseCode")
    )

    return null
}
