package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(name = "CalendarWithBeer", description = "En kalender med et øl og dets kalenderdag.")
data class CalendarWithBeer(
    @field:NotNull var id: UUID, @field:NotNull var name: String, @field:NotNull var year: Int,
    @field:NotNull var published: Boolean, @field:NotNull var archived: Boolean,
    @field:NotNull var beerCalendars: Set<BeerCalendar>,
    @field:NotNull var calendarToken: CalendarToken, @field:NotNull var beer: Beer,
    @field:NotNull var day: Int
)
