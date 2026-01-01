package no.juleoelkalender.controller

import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.model.Review
import no.juleoelkalender.model.ReviewData
import no.juleoelkalender.model.ReviewWithUser
import no.juleoelkalender.service.LocalesService
import no.juleoelkalender.service.ReviewService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/review")
class ReviewController(private val reviewService: ReviewService, private val localesService: LocalesService) {
    @get:PreAuthorize("hasAuthority('review:read')")
    @get:GetMapping
    val reviews: ResponseEntity<Set<ReviewWithUser>>
        get() = ResponseEntity.ok(reviewService.reviewsWithUser)

    @GetMapping(path = ["/export/{locale}"])
    @PreAuthorize("hasAuthority('review:read')")
    fun getAllReviewsExport(@PathVariable locale: String): ResponseEntity<ByteArray> {
        val fileName = localesService.getString(locale, "pages.beerreview.excelexportfilename")
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .body(reviewService.getReviewsXlsx(locale))
    }

    @GetMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('review:read')")
    fun getReviewById(@PathVariable id: UUID): ResponseEntity<Review> {
        val review = reviewService.getById(id)
                ?: throw NotFoundException("Review with id $id not found")
        return ResponseEntity.ok(review)
    }

    @GetMapping(path = ["/bycalendarbeerandreviewer/{calendarId}/{beerId}/{reviewerId}"])
    @PreAuthorize("hasAuthority('review:read')")
    fun getReviewByCalendarBeerAndReviewer(
            @PathVariable calendarId: UUID,
            @PathVariable beerId: UUID, @PathVariable reviewerId: UUID
    ): ResponseEntity<Review> {
        return ResponseEntity.ok(
                reviewService.getReviewByCalendarBeerAndReviewer(calendarId, beerId, reviewerId)
        )
    }

    @GetMapping(path = ["/reviewdata/{id}"])
    @PreAuthorize("hasAuthority('review:read')")
    fun getReviewDataByBeerId(@PathVariable id: UUID): ResponseEntity<Set<ReviewData>> {
        return ResponseEntity.ok(reviewService.getReviewDataByBeerId(id))
    }

    @PostMapping
    @PreAuthorize("hasAuthority('review:create')")
    @Throws(URISyntaxException::class)
    fun createReview(@RequestBody review: Review): ResponseEntity<Review> {
        return ResponseEntity.created(URI("/beer/reviews")).body(reviewService.create(review))
    }

    @PutMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('review:update') or (#review.user.email != authentication.principal.username and hasAuthority('review:update_other'))")
    fun updateReview(@PathVariable id: UUID, @RequestBody review: Review): ResponseEntity<Review> {
        val updated = reviewService.update(id, review)
                ?: throw NotFoundException("Review with id $id not found")
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('review:delete')")
    fun deleteReview(@PathVariable id: UUID): ResponseEntity<Boolean> {
        reviewService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
