package no.juleoelkalender.repository

import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.entity.ReviewEntity
import no.juleoelkalender.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReviewRepository : JpaRepository<ReviewEntity, UUID> {
    fun findByCalendar_CalendarToken_Id(calendarTokenId: UUID): List<ReviewEntity>
    fun findByBeer_Id(beerId: UUID): List<ReviewEntity>

    fun findByBeerAndCalendarAndUser(beer: BeerEntity, calendar: CalendarEntity, user: UserEntity): ReviewEntity?
}
