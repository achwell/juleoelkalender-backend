package no.juleoelkalender.controller

import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.model.Calendar
import no.juleoelkalender.model.CalendarWithBeer
import no.juleoelkalender.service.CalendarService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/calendar")
class CalendarController(private val calendarService: CalendarService) {
    @get:PreAuthorize("hasAuthority('calendar:read')")
    @get:GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    val calendars: ResponseEntity<Set<Calendar>>
        get() = ResponseEntity.ok(calendarService.all)

    @GetMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('calendar:read')")
    fun getCalendarById(@PathVariable id: UUID): ResponseEntity<Calendar> {
        val calendar = calendarService.getById(id)
                ?: throw NotFoundException("Calendar with id $id not found")
        return ResponseEntity.ok(calendar)
    }

    @GetMapping(path = ["/beer/{calendarId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('calendar:read')")
    fun getCalendarWithBeers(
            @PathVariable calendarId: UUID
    ): ResponseEntity<Set<CalendarWithBeer>> {
        return ResponseEntity.ok(calendarService.getCalendarWithBeers(calendarId))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('calendar:create')")
    @Throws(URISyntaxException::class)
    fun createCalendar(@RequestBody calendar: Calendar): ResponseEntity<Calendar> {
        return ResponseEntity.created(URI("/calendar")).body(calendarService.create(calendar))
    }

    @PutMapping(path = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('calendar:update')")
    fun updateCalendar(
            @PathVariable id: UUID,
            @RequestBody calendar: Calendar
    ): ResponseEntity<Calendar> {
        val updated = calendarService.update(id, calendar)
                ?: throw NotFoundException("Calendar with id $id not found")
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('calendar:delete')")
    fun deleteCalendar(@PathVariable id: UUID): ResponseEntity<Boolean> {
        calendarService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
