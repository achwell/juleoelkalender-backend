package no.juleoelkalender.controller

import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.BeerWithCalendarAndDay
import no.juleoelkalender.model.BeerWithCalendarDayAndReview
import no.juleoelkalender.service.BeerService
import no.juleoelkalender.service.LocalesService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/v1/beer")
class BeerController(private val beerService: BeerService, private val localesService: LocalesService) {
    @get:PreAuthorize("hasAuthority('beer:read')")
    @get:GetMapping
    val beers: ResponseEntity<Set<Beer>>
        get() = ResponseEntity.ok(beerService.all)

    @GetMapping(path = ["/review/{calendarId}"])
    @PreAuthorize("hasAuthority('beer:read')")
    fun getBeersWithReviewByCalendar(
            @PathVariable calendarId: UUID
    ): ResponseEntity<Set<BeerWithCalendarDayAndReview>> {
        return ResponseEntity.ok(beerService.getBeersWithReviewByCalendarAndUser(calendarId, null))
    }

    @GetMapping(path = ["/review/{calendarId}/{userId}"])
    @PreAuthorize("hasAuthority('beer:read')")
    fun getBeersWithReviewByCalendarAndUser(
            @PathVariable calendarId: UUID, @PathVariable userId: UUID
    ): ResponseEntity<Set<BeerWithCalendarDayAndReview>> {
        return ResponseEntity.ok(beerService.getBeersWithReviewByCalendarAndUser(calendarId, userId))
    }

    @get:PreAuthorize("hasAuthority('beer:read')")
    @get:GetMapping(path = ["/allcalendar"])
    val allBeersWithCalendar: ResponseEntity<Set<BeerWithCalendarAndDay>>
        get() = ResponseEntity.ok(beerService.getBeersWithCalendar(null, null))

    @GetMapping(path = ["/allcalendarexport/{locale}"])
    @PreAuthorize("hasAuthority('beer:read')")
    fun getAllBeersWithCalendarExport(@PathVariable locale: String): ResponseEntity<ByteArray> {
        val fileName = localesService.getString(locale, "pages.beeradmin.excelexportfilename")
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .body(beerService.getBeersWithCalendarXlsx(null, null, locale))
    }

    @get:PreAuthorize("hasAuthority('beer:read')")
    @get:GetMapping(path = ["/today"])
    val todaysBeersWithCalendarAndReview: ResponseEntity<Set<BeerWithCalendarDayAndReview>>
        get() = ResponseEntity.ok(beerService.getBeersWithCalendarAndReviewByDate(LocalDate.now()))

    @get:PreAuthorize("hasAuthority('beer:read')")
    @get:GetMapping(path = ["/calendar"])
    val beersWithCalendar: ResponseEntity<Set<BeerWithCalendarAndDay>>
        get() {
            val email = SecurityContextHolder.getContext().authentication!!.principal as String
            return ResponseEntity.ok(beerService.getBeersWithCalendar(null, email))
        }

    @GetMapping(path = ["/calendar/{calendarId}"])
    @PreAuthorize("hasAuthority('beer:read')")
    fun getBeersWithCalendarByCalendar(
            @PathVariable calendarId: UUID
    ): ResponseEntity<Set<BeerWithCalendarAndDay>> {
        return ResponseEntity.ok(beerService.getBeersWithCalendar(calendarId, null))
    }

    @GetMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('beer:read')")
    fun getBeerById(@PathVariable id: UUID): ResponseEntity<Beer> {
        val beer = beerService.getById(id) ?: throw NotFoundException()
        return ResponseEntity.ok(beer)
    }

    @PostMapping
    @PreAuthorize("hasAuthority('beer:create')")
    @Throws(URISyntaxException::class)
    fun createBeer(@RequestBody beer: Beer): ResponseEntity<Beer> {
        return ResponseEntity.created(URI("/beers")).body(beerService.create(beer))
    }

    @PutMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('beer:update') or (#beer.email != authentication.principal.username and hasAuthority('beer:update_other'))")
    fun updateBeer(@PathVariable id: UUID, @RequestBody beer: Beer): ResponseEntity<Beer> {
        val updated = beerService.update(id, beer) ?: throw NotFoundException()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('beer:delete')")
    fun deleteBeer(@PathVariable id: UUID): ResponseEntity<Boolean> {
        beerService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
