package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "Anmeldelser for en kalender og en gjennomsnittsanmeldelse.")
data class ReviewData(
    @field:NotNull var reviews: Set<Review>, @field:NotNull var calendar: Calendar, @field:NotNull var average: Review
)
