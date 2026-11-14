package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(name = "BeerWithCalendarDayAndReview", description = "Et øl med kalenderdag, brygger og eventuell anmeldelse.")
data class BeerWithCalendarDayAndReview(
    @field:NotNull var beer: Beer, @field:NotNull var calendar: Calendar,
    @field:NotNull var brewer: UserWithoutChildren,
    @field:NotNull var day: Int,
    @field:Schema(description = "Anmeldelsen av ølet.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val review: Review?
) 