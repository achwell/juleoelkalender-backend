package no.juleoelkalender.controller

import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.model.CalendarToken
import no.juleoelkalender.service.CalendarTokenService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/calendartoken")
class CalendarTokenController(private val calendarTokenService: CalendarTokenService) {
    @get:PreAuthorize("hasAuthority('calendartoken:read')")
    @get:GetMapping
    val calendarTokens: ResponseEntity<Set<CalendarToken>>
        get() = ResponseEntity.ok(calendarTokenService.all)

    @GetMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('calendartoken:read')")
    fun getCalendarTokenById(@PathVariable id: UUID): ResponseEntity<CalendarToken> {
        val calendarToken = calendarTokenService.getById(id)
                ?: throw NotFoundException("CalendarToken with id $id not found")
        return ResponseEntity.ok(calendarToken)
    }

    @PostMapping
    @PreAuthorize("hasAuthority('calendartoken:create')")
    @Throws(URISyntaxException::class)
    fun createCalendarToken(@RequestBody calendarToken: CalendarToken): ResponseEntity<CalendarToken> {
        return ResponseEntity.created(URI("/admin/calendartoken")).body(calendarTokenService.create(calendarToken))
    }

    @PutMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('calendartoken:update')")
    fun updateCalendarToken(
            @PathVariable id: UUID,
            @RequestBody calendarToken: CalendarToken
    ): ResponseEntity<CalendarToken> {
        val updated = calendarTokenService.update(id, calendarToken)
                ?: throw NotFoundException("CalendarToken with id $id not found")
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('calendartoken:delete')")
    fun deleteCalendarToken(@PathVariable id: UUID): ResponseEntity<Boolean> {
        calendarTokenService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
