package org.example.kufar.db

import com.mongodb.client.model.Filters.eq
import org.bson.Document

fun insertOrUpdate(document: Document) {
    val collection = getMongoCollection()
    val filter = eq("product_id", document.get("product_id"))
    val find = collection?.find(filter)

    if (find?.first() == null) {
        println("inserted: " + document["product_id"])
        collection?.insertOne(document)
    } else {
        println("updated product: " + document["product_id"])
        collection.updateOne(filter, Document("\$set", document))
    }
}

fun insertOrUpdateTimer(document: Document) {
    val collection = getMongoCollection(TIMER_COLLECTION_NAME)
    val filter = eq("chat_id", document.get("chat_id"))
    val find = collection?.find(filter)

    if (find?.first() == null) {
        println("inserted timer to chat_id: " + document["chat_id"])
        collection?.insertOne(document)
    } else {
        println("updated timer: " + document["chat_id"])
        collection.updateOne(filter, Document("\$set", document))
    }
}

fun insertOrUpdate(documents: List<Document>) {
    documents.forEach { insertOrUpdate(it) }
}