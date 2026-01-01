package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.BeerCalendarEntity
import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.mappers.BeerCalendarMapper
import no.juleoelkalender.model.BeerCalendar
import no.juleoelkalender.model.Direction
import no.juleoelkalender.repository.BeerCalendarRepository
import no.juleoelkalender.repository.BeerRepository
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.service.BeerCalendarService
import org.springframework.stereotype.Service
import java.util.Objects
import java.util.UUID

@Service
class BeerCalendarServiceImpl(
        private val beerCalendarRepository: BeerCalendarRepository, private val beerRepository: BeerRepository,
        private val calendarRepository: CalendarRepository, mapper: BeerCalendarMapper
) : BaseServiceImpl<UUID, BeerCalendar, BeerCalendarEntity>(beerCalendarRepository, mapper), BeerCalendarService {

    override fun preCreate(model: BeerCalendar): BeerCalendarEntity {
        return mapper.modelToEntity(model).apply {
            beer = beerRepository.getReferenceById(model.beer.id)
            calendar = calendarRepository.getReferenceById(model.calendar.id!!)
        }
    }

    override fun mapModelToEntity(model: BeerCalendar, entity: BeerCalendarEntity) {
        entity.calendar = calendarRepository.getReferenceById(model.calendar.id!!)
        entity.beer = beerRepository.getReferenceById(model.beer.id)
        entity.day = model.day
    }

    override fun moveBeerCalendar(calendarId: UUID, day: Int, direction: Direction) {
        val beerCalendar1 = beerCalendarRepository.findByDayAndCalendar_Id(
                day,
                calendarId
        )
        if (beerCalendar1 == null) {
            throw NotFoundException("BeerCalendar with id $day not found")
        }
        if (Objects.requireNonNull<Direction?>(direction) == Direction.UP) {
            if (day > 1) {
                beerCalendar1.day = day - 1
                val beerCalendarEntity = beerCalendarRepository.findByDayAndCalendar_Id(day - 1, calendarId)
                if (beerCalendarEntity != null) {
                    beerCalendarEntity.day = day
                    repository.save(beerCalendarEntity)
                }
                repository.save(beerCalendar1)
            }
        } else if (direction == Direction.DOWN) {
            if (day >= 24) {
                return
            }
            beerCalendar1.day = day + 1
            val beerCalendarEntity = beerCalendarRepository.findByDayAndCalendar_Id(day + 1, calendarId)
            if (beerCalendarEntity != null) {
                beerCalendarEntity.day = day
                repository.save(beerCalendarEntity)
            }
            repository.save(beerCalendar1)
        }
    }
}