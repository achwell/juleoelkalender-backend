package no.juleoelkalender.service

import no.juleoelkalender.model.Calendar
import no.juleoelkalender.model.CalendarWithBeer
import java.util.UUID

interface CalendarService : BaseService<UUID, Calendar> {
    fun getCalendarWithBeers(calendarId: UUID): Set<CalendarWithBeer>
}