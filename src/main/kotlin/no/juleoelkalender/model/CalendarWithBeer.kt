package no.juleoelkalender.model

import java.util.UUID

data class CalendarWithBeer(
        val id: UUID, val name: String, val year: Int,
        val published: Boolean, val archived: Boolean,
        val beerCalendars: Set<BeerCalendar>,
        val calendarToken: CalendarToken, val beer: Beer,
        val day: Int
)
