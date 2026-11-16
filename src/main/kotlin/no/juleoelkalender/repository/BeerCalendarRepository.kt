package no.juleoelkalender.repository

import no.juleoelkalender.entity.BeerCalendarEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface BeerCalendarRepository : JpaRepository<BeerCalendarEntity, UUID> {
    fun findByDayAndCalendar_Id(day: Int, calendarId: UUID): BeerCalendarEntity?

    @Query(
        "select count(beerCalendar) from BeerCalendarEntity beerCalendar " +
                "where beerCalendar.calendar.year = :year " +
                "and beerCalendar.calendar.archived = false " +
                "and beerCalendar.beer.archived = false"
    )
    fun countActiveBeerPlacementsForYear(@Param("year") year: Int): Long
}
