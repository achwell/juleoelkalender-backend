package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(name = "BeerCalendar", description = "Kobling mellom et øl og en dag i en kalender.")
data class BeerCalendar(
    @field:NotNull var id: UUID,
    @field:Schema(description = "Kalenderdagen ølet er plassert på.", example = "24")
    @field:NotNull var day: Int,
    @field:Schema(implementation = Beer::class)
    @field:NotNull var beer: Beer,
    @field:Schema(implementation = Calendar::class)
    @field:NotNull var calendar: Calendar
) 