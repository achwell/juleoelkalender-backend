package no.juleoelkalender.service

import no.juleoelkalender.model.*
import java.util.UUID

interface ReviewService : BaseService<UUID, Review> {
    val reviewsWithUser: Set<ReviewWithUser>

    fun getReviewByCalendarBeerAndReviewer(calendarId: UUID, beerId: UUID, reviewerId: UUID): Review

    fun getReviewDataByBeerId(beerId: UUID): Set<ReviewData>

    fun calculateAverage(reviews: Collection<Review>, calendar: Calendar, beer: Beer, user: UserWithoutChildren): Review

    fun getReviewsXlsx(locale: String): ByteArray
}