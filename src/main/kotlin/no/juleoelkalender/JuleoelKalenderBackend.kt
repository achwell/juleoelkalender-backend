package no.juleoelkalender

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JuleoelKalenderBackend

fun main(args: Array<String>) {
    runApplication<JuleoelKalenderBackend>(*args)
}