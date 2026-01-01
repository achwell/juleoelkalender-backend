package no.juleoelkalender.controller

import no.juleoelkalender.model.Device
import no.juleoelkalender.service.DeviceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/log")
class LogController(private val deviceService: DeviceService) {
    @PostMapping("/{logLevel}")
    fun log(@RequestBody logLine: String, @PathVariable logLevel: String): ResponseEntity<Void> {
        when (logLevel) {
            "trace" -> {
                log.trace(logLine)
            }

            "debug" -> {
                log.debug(logLine)
            }

            "info" -> {
                log.info(logLine)
            }

            "warn" -> {
                log.warn(logLine)
            }

            "error" -> {
                log.error(logLine)
            }
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("logDevice")
    fun logDevice(@RequestBody device: Device): ResponseEntity<Void> {
        val newDevice = deviceService.create(device)
        log.info("Login: {}", newDevice.toString())
        return ResponseEntity.noContent().build()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(LogController::class.java)
    }
}
