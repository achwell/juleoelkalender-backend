package no.juleoelkalender.controller

import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.model.BeerCalendar
import no.juleoelkalender.model.Direction
import no.juleoelkalender.service.BeerCalendarService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/beercalendar")
class BeerCalendarController(private val beerCalendarService: BeerCalendarService) {
    @get:PreAuthorize("hasAuthority('beercalendar:read')")
    @get:GetMapping
    val beerCalendars: ResponseEntity<Set<BeerCalendar>>
        get() = ResponseEntity.ok(beerCalendarService.all)

    @GetMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('beercalendar:read')")
    fun getBeerCalendarById(@PathVariable id: UUID): ResponseEntity<BeerCalendar> {
        val beerCalendar = beerCalendarService.getById(id)
                ?: throw NotFoundException("BeerCalendar with id $id not found")
        return ResponseEntity.ok(beerCalendar)
    }

    @PostMapping
    @PreAuthorize("hasAuthority('beercalendar:create')")
    @Throws(URISyntaxException::class)
    fun createBeerCalendar(@RequestBody beerCalendar: BeerCalendar): ResponseEntity<BeerCalendar> {
        return ResponseEntity.created(URI("/calendar")).body(beerCalendarService.create(beerCalendar))
    }

    @PostMapping(path = ["/move/{calendarId}/{day}/{direction}"])
    @PreAuthorize("hasAuthority('beercalendar:update')")
    fun moveBeerCalendar(
            @PathVariable calendarId: UUID,
            @PathVariable day: Int, @PathVariable direction: Direction
    ): ResponseEntity<Void> {
        beerCalendarService.moveBeerCalendar(calendarId, day, direction)
        return ResponseEntity.noContent().build()
    }

    @PutMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('beercalendar:update')")
    fun updateBeerCalendar(
            @PathVariable id: UUID,
            @RequestBody beerCalendar: BeerCalendar
    ): ResponseEntity<BeerCalendar> {
        val updated = beerCalendarService.update(id, beerCalendar)
                ?: throw NotFoundException("BeerCalendar with id $id not found")
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('beercalendar:delete')")
    fun deleteBeerCalendar(@PathVariable id: UUID): ResponseEntity<Boolean> {
        beerCalendarService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
