package org.example.kufar.db

import com.mongodb.client.model.Filters.eq
import org.bson.Document

fun insertOrUpdate(document: Document) {
    val filter = eq("product_id", document.get("product_id"))
    val collection = getMongoCollection()
    val find = collection?.find(filter)

    if (find == null) {
        println("inserted: " + document["product_id"])
        collection?.insertOne(document)
    } else {
        println("updated product: " + document["product_id"])
        collection.updateOne(filter, Document("\$set", document))
    }
}

fun insertOrUpdate(documents: List<Document>) {
    documents.forEach { insertOrUpdate(it) }
}