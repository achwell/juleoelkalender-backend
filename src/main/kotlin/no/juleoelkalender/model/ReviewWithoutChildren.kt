package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime
import java.util.UUID

@Schema(description = "En anmeldelse uten tilknyttede objekter.")
data class ReviewWithoutChildren(
    val id: UUID, val ratingLabel: Double,
    val ratingLooks: Double, val ratingSmell: Double,
    val ratingTaste: Double, val ratingFeel: Double,
    val ratingOverall: Double,
    @field:Schema(description = "Fritekst om ølet.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val comment: String?,
    val createdAt: ZonedDateTime
)
