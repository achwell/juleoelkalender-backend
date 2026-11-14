package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime
import java.util.UUID

@Schema(description = "En brukers anmeldelse av et øl i en kalender.")
data class Review(
    @field:Schema(description = "Identifikator for anmeldelsen.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var id: UUID?,
    @field:NotNull var ratingLabel: Double,
    @field:NotNull var ratingLooks: Double,
    @field:NotNull var ratingSmell: Double,
    @field:NotNull var ratingTaste: Double,
    @field:NotNull var ratingFeel: Double,
    @field:NotNull var ratingOverall: Double,
    @field:Schema(description = "Fritekst om ølet.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var comment: String?,
    @field:NotNull var createdAt: ZonedDateTime,
    @field:NotNull var beer: Beer,
    @field:NotNull var calendar: Calendar,
    @field:NotNull var user: UserWithoutChildren
) {

    fun updateRatings(review: Review) {
        this.ratingLabel = review.ratingLabel
        this.ratingLooks = review.ratingLooks
        this.ratingSmell = review.ratingSmell
        this.ratingTaste = review.ratingTaste
        this.ratingFeel = review.ratingFeel
        this.ratingOverall = review.ratingOverall
    }
}
