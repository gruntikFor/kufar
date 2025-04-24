package org.example.kufar.model

data class Data(val items: List<Item>) {

    data class Item(val id: String, val auto_names: Title, val counters: Counters)
    data class Counters(val new: Int)
    data class Title(val ru: String) {
        private val upRu: String get() = ru.uppercase()

        override fun toString(): String {
            return "Title(ru='$upRu')"
        }
    }
}