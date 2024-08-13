package org.example.kufar.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Data(@JsonProperty("items") val items: List<Item>) {
    data class Item(@JsonProperty("id") val id: String, @JsonProperty("counters") val counters: Counters)

    data class Counters(@JsonProperty("new") val new: Int)
}