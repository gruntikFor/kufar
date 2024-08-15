package org.example.kufar.utils

import java.net.HttpURLConnection
import java.net.URL

fun simplePost(url: String, pair: Pair<String, String>): Int {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty(pair.first, pair.second)

    return connection.responseCode
}