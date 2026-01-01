package no.juleoelkalender.service

import no.juleoelkalender.model.BeerCalendar
import no.juleoelkalender.model.Direction
import java.util.UUID

interface BeerCalendarService : BaseService<UUID, BeerCalendar> {
    fun moveBeerCalendar(calendarId: UUID, day: Int, direction: Direction)
}