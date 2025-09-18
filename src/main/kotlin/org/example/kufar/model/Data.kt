package org.example.kufar.model

data class Data(val items: List<Item>) {

    data class Item(val id: String, val auto_names: Title, val query: String, val counters: Counters) {
        override fun toString(): String {
            return "title: ${auto_names.ru}, counter: ${counters.new}"
        }
    }

    data class Counters(val new: Int)
    data class Title(val ru: String) {
        val upRu: String get() = ru.uppercase()

//        override fun toString(): String {
//            return "Title(ru='$upRu')"
//        }
    }

    override fun toString(): String {
        return "Data(items=$items)"
    }
}