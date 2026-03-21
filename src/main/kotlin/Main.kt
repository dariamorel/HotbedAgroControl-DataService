package org.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class DataServiceApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<DataServiceApplication>(*args)
        }
    }
}