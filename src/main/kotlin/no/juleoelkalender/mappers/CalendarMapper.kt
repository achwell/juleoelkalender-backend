package no.juleoelkalender.mappers

import no.juleoelkalender.entity.BeerCalendarEntity
import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.model.BeerCalendar
import no.juleoelkalender.model.Calendar
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class CalendarMapper(private val beerMapper: BeerMapper, private val calendarTokenMapper: CalendarTokenMapper) : BaseMapper<Calendar, CalendarEntity> {
    override fun entityToModel(entity: CalendarEntity): Calendar {
        val beerCalendars = entity.beerCalendars.map { BeerCalendar(it.id, it.day, beerMapper.entityToModel(it.beer), calendarEntityToCalendar(it.calendar)) }.toSet()
        return Calendar(entity.id, entity.name, entity.year, entity.published, entity.archived, beerCalendars, calendarTokenMapper.entityToModel(entity.calendarToken))
    }

    override fun modelToEntity(model: Calendar): CalendarEntity {
        return CalendarEntity(model.id, model.name, model.year, model.published, model.archived, model.beerCalendars.map {
            BeerCalendarEntity(it.id, it.day, beerMapper.modelToEntity(it.beer), calendarToCalendarEntity(it.calendar))
        }.toMutableSet(), calendarTokenMapper.modelToEntity(model.calendarToken), mutableSetOf(), ZonedDateTime.now(), null)
    }

    private fun calendarEntityToCalendar(calendar: CalendarEntity): Calendar {
        return Calendar(calendar.id, calendar.name, calendar.year, calendar.published, calendar.archived, mutableSetOf(), calendarTokenMapper.entityToModel(calendar.calendarToken))
    }

    private fun calendarToCalendarEntity(calendar: Calendar): CalendarEntity {
        return CalendarEntity(calendar.id, calendar.name, calendar.year, calendar.published, calendar.archived, mutableSetOf(), calendarTokenMapper.modelToEntity(calendar.calendarToken), mutableSetOf(), ZonedDateTime.now(), null)
    }
}
