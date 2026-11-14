package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(name = "BeerWithCalendarAndDay", description = "Et øl med brygger, kalenderdag og eventuell kalender.")
data class BeerWithCalendarAndDay(
    @field:NotNull var beer: Beer,
    @field:Schema(description = "Kalenderen ølet er plassert i.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val calendar: Calendar?,
    @field:NotNull var brewer: UserWithoutChildren, @field:NotNull var day: Int
)
