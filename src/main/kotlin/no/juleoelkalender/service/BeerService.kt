package no.juleoelkalender.service

import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.BeerWithCalendarAndDay
import no.juleoelkalender.model.BeerWithCalendarDayAndReview
import java.time.LocalDate
import java.util.UUID

interface BeerService : BaseService<UUID, Beer> {
    fun getBeersWithReviewByCalendarAndUser(calendarId: UUID, userId: UUID?): Set<BeerWithCalendarDayAndReview>

    fun getBeersWithCalendar(calendarId: UUID?, email: String?): Set<BeerWithCalendarAndDay>

    fun getBeersWithCalendarXlsx(calendarId: UUID?, email: String?, locale: String): ByteArray

    fun getBeersWithCalendarAndReviewByDate(date: LocalDate): Set<BeerWithCalendarDayAndReview>
}