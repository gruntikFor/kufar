package org.example.kufar.db

data class DBData(
    val chat_id: String,
    val product_id: String,
    val title: String,
    val query: String,
    val show: Boolean,
    val index: Int
)