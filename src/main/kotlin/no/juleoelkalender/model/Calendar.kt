package no.juleoelkalender.model

import java.util.UUID

data class Calendar(
        val id: UUID?, val name: String, val year: Int,
        val published: Boolean, val archived: Boolean,
        val beerCalendars: Set<BeerCalendar>,
        val calendarToken: CalendarToken
)
