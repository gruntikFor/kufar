package org.example.kufar.db

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document

private const val URL = "mongodb://myuser:mypassword@localhost:27017"
private const val DB_NAME = "kufar"
private const val COLLECTION_NAME = "test"
var client: MongoClient? = null
var collections: MongoCollection<Document>? = null

fun initMongoClient() {
    println("initMongoClient()")
    if (client == null) {
        client = MongoClients.create(URL)
        val database = client?.getDatabase(DB_NAME)
        collections = database?.getCollection(COLLECTION_NAME)
    }
}

fun getMongoCollection(): MongoCollection<Document>? {
    return collections
}

fun destroyMongoConnection() {
    client?.close()
}

fun insert(document: Document) {
    getMongoCollection()?.insertOne(document)
}

fun insertMany(documents: List<Document>) {
    getMongoCollection()?.insertMany(documents)
}

fun find(): FindIterable<Document?> {
    collections?.let { return it.find() }
    throw RuntimeException("Mongo Collection Not Found")
}