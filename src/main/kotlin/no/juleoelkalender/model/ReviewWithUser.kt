package no.juleoelkalender.model

import java.time.ZonedDateTime
import java.util.UUID

data class ReviewWithUser(
        val id: UUID, val ratingLabel: Double,
        val ratingLooks: Double, val ratingSmell: Double,
        val ratingTaste: Double, val ratingFeel: Double,
        val ratingOverall: Double, val comment: String?,
        val createdAt: ZonedDateTime, val beer: Beer,
        val calendar: Calendar, val user: UserWithoutChildren,
        val total: Double
)
