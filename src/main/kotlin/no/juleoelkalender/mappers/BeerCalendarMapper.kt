package no.juleoelkalender.mappers

import no.juleoelkalender.entity.BeerCalendarEntity
import no.juleoelkalender.model.BeerCalendar
import org.springframework.stereotype.Component

@Component
class BeerCalendarMapper(private val beerMapper: BeerMapper, private val calendarMapper: CalendarMapper) : BaseMapper<BeerCalendar, BeerCalendarEntity> {
    override fun entityToModel(entity: BeerCalendarEntity): BeerCalendar = BeerCalendar(
            entity.id, entity.day,
            beerMapper.entityToModel(entity.beer),
            calendarMapper.entityToModel(entity.calendar)
    )

    override fun modelToEntity(model: BeerCalendar): BeerCalendarEntity = BeerCalendarEntity(
            model.id, model.day,
            beerMapper.modelToEntity(model.beer),
            calendarMapper.modelToEntity(model.calendar)
    )
}
