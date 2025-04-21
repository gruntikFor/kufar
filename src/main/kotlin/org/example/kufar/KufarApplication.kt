package org.example.kufar

import org.example.kufar.configuration.Configuration
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
}

fun main(args: Array<String>) {
    runApplication<KufarApplication>(*args)
}