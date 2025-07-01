package org.example.kufar

import jakarta.annotation.PreDestroy
import org.example.kufar.configuration.Configuration
import org.example.kufar.db.destroyMongoConnection
import org.example.kufar.db.initMongoClient
import org.example.kufar.runner.Runner
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

val LOGGER = LoggerFactory.getLogger("Kufar")

@SpringBootApplication
class KufarApplication @Autowired constructor(val configuration: Configuration, val runner: Runner) :
    CommandLineRunner {

    override fun run(vararg args: String) {
        configuration.init()
        runner.run()
    }

    init {
        initMongoClient()
    }

    @PreDestroy
    fun preDestroy() {
        destroyMongoConnection()
    }
}

fun main(args: Array<String>) {
    runApplication<KufarApplication>(*args)
}