package no.juleoelkalender.controller

import no.juleoelkalender.service.LocalesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.json.JsonMapper
import java.io.IOException

@RestController
@RequestMapping("/locales")
class LocalesController(private val localesService: LocalesService, private val jsonMapper: JsonMapper) {
    @GetMapping(path = ["/{locale}/translation.json"])
    fun getLocale(@PathVariable locale: String): ResponseEntity<String> {
        try {
            return ResponseEntity.ok(jsonMapper.writeValueAsString(localesService.getLocale(locale)))
        } catch (e: IOException) {
            log.error("Error getting locale for {}", locale, e)
            return ResponseEntity.notFound().build()
        }
    }

    @PostMapping(path = ["/{locale}/missing.json"])
    fun addMissing(@PathVariable locale: String, @RequestBody data: Map<String, String>) {
        localesService.addMissing(locale, data)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(LocalesController::class.java)
    }
}
