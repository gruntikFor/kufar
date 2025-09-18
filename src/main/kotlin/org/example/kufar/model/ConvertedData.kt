package org.example.kufar.model

data class ConvertedData(
    val product_id: String,
    val title: String,
    var query: String,
    var oldCount: Int,
    var count: Int
) {
    override fun toString(): String {
        return "\nConvertedData(product_id='$product_id', title='$title', oldCount=$oldCount, count=$count)"
    }
}