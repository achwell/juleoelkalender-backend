package no.juleoelkalender.model

import java.time.ZonedDateTime
import java.util.UUID

data class Review(var id: UUID?, var ratingLabel: Double, var ratingLooks: Double, var ratingSmell: Double, var ratingTaste: Double, var ratingFeel: Double, var ratingOverall: Double, var comment: String?, var createdAt: ZonedDateTime, var beer: Beer, var calendar: Calendar, var user: UserWithoutChildren) {

    fun updateRatings(review: Review) {
        this.ratingLabel = review.ratingLabel
        this.ratingLooks = review.ratingLooks
        this.ratingSmell = review.ratingSmell
        this.ratingTaste = review.ratingTaste
        this.ratingFeel = review.ratingFeel
        this.ratingOverall = review.ratingOverall
    }
}
