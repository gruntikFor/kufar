package org.example.kufar.db

data class DBData(
    val chat_id: String,
    val product_id: String,
    val title: String,
    var query: String,
    var show: Boolean,
    val index: Int,
    var count: Int
)