package org.example.kufar.utils

import com.google.gson.Gson
import org.example.kufar.LOGGER
import org.example.kufar.model.Data
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private val POST = "POST"
private val GET = "GET"

fun simplePost(url: String, pair: Pair<String, String>): Int {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = POST
    connection.setRequestProperty(pair.first, pair.second)

    return connection.responseCode
}

fun simpleGet(url: String, pair: Pair<String, String>): Data {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = GET
    connection.setRequestProperty(pair.first, pair.second)

    val inputReader = BufferedReader(InputStreamReader(connection.inputStream))
    val jsonString = inputReader.use { it.readText() }
    val data = Gson().fromJson(jsonString, Data::class.java)
    LOGGER.info(data.toString())

    return data
}

