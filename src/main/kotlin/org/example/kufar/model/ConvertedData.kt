package org.example.kufar.model

data class ConvertedData(
    val product_id: String,
    val title: String,
    var query: String,
    var oldCount: Int,
    var count: Int
)