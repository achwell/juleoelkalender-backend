package no.juleoelkalender.controller

import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.model.BeerStyle
import no.juleoelkalender.service.BeerStyleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/beerstyle")
class BeerStyleController(private val beerStyleService: BeerStyleService) {

    @get:PreAuthorize("isAuthenticated()")
    @get:GetMapping
    val beerStyles: ResponseEntity<Set<BeerStyle>>
        get() = ResponseEntity.ok(beerStyleService.all)

    @GetMapping(path = ["/{id}"])
    @PreAuthorize("isAuthenticated()")
    fun getBeerStyleById(@PathVariable id: UUID): ResponseEntity<BeerStyle> {
        val beerStyle = beerStyleService.getById(id)
                ?: throw NotFoundException("BeerStyle with id $id not found")
        return ResponseEntity.ok(beerStyle)
    }

    @PostMapping
    @PreAuthorize("hasAuthority('beerstyle:create')")
    @Throws(URISyntaxException::class)
    fun createBeerStyle(@RequestBody beerStyle: BeerStyle): ResponseEntity<BeerStyle> {
        return ResponseEntity.created(URI("/beers")).body<BeerStyle>(beerStyleService.create(beerStyle))
    }

    @PutMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('beerstyle:update')")
    fun updateBeerStyle(
            @PathVariable id: UUID,
            @RequestBody beerStyle: BeerStyle
    ): ResponseEntity<BeerStyle> {
        val updated = beerStyleService.update(id, beerStyle)
                ?: throw NotFoundException("BeerStyle with id $id not found")
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('beerstyle:delete')")
    fun deleteBeerStyle(@PathVariable id: UUID): ResponseEntity<Boolean> {
        beerStyleService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}