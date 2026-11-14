package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime
import java.util.UUID

@Schema(description = "En anmeldelse med bruker, øl og kalender.")
data class ReviewWithUser(
    @field:NotNull var id: UUID, val ratingLabel: Double,
    @field:NotNull var ratingLooks: Double, val ratingSmell: Double,
    @field:NotNull var ratingTaste: Double, val ratingFeel: Double,
    @field:NotNull var ratingOverall: Double,
    @field:Schema(description = "Fritekst om ølet.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val comment: String?,
    @field:NotNull var createdAt: ZonedDateTime,
    @field:NotNull var beer: Beer,
    @field:NotNull var calendar: Calendar,
    @field:NotNull var user: UserWithoutChildren,
    @field:NotNull var total: Double
)
