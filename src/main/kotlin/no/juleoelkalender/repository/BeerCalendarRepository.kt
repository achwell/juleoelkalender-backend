package no.juleoelkalender.repository

import no.juleoelkalender.entity.BeerCalendarEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BeerCalendarRepository : JpaRepository<BeerCalendarEntity, UUID> {
    fun findByDayAndCalendar_Id(day: Int, calendarId: UUID): BeerCalendarEntity?
}
