package no.juleoelkalender.repository

import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.entity.CalendarTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CalendarRepository : JpaRepository<CalendarEntity, UUID> {
    fun findByCalendarToken(calendarToken: CalendarTokenEntity): Collection<CalendarEntity>
}
