package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.BeerCalendarEntity
import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.mappers.BeerCalendarMapper
import no.juleoelkalender.mappers.BeerMapper
import no.juleoelkalender.mappers.CalendarMapper
import no.juleoelkalender.mappers.CalendarTokenMapper
import no.juleoelkalender.model.BeerCalendar
import no.juleoelkalender.model.Calendar
import no.juleoelkalender.model.CalendarWithBeer
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.CalendarService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.stream.Collectors
import kotlin.jvm.optionals.getOrNull
import kotlin.jvm.optionals.toSet

@Service
class CalendarServiceImpl(
        repository: CalendarRepository, private val userRepository: UserRepository,
        private val beerMapper: BeerMapper, private val beerCalendarMapper: BeerCalendarMapper, mapper: CalendarMapper,
        private val calendarTokenMapper: CalendarTokenMapper
) : BaseServiceImpl<UUID, Calendar, CalendarEntity>(repository, mapper), CalendarService {

    override val all: Set<Calendar>
        get() = repository.findAll().map { mapper.entityToModel(it) }.toSet()

    override fun getById(id: UUID): Calendar? {
        return repository.findById(id).map { mapper.entityToModel(it) }.getOrNull()
    }

    override fun getCalendarWithBeers(calendarId: UUID): Set<CalendarWithBeer> {
        val calendarWithBeers: MutableSet<CalendarWithBeer> = mutableSetOf()
        repository.findById(calendarId).map {
            for (beerCalendar in it.beerCalendars) {
                val beer = beerCalendar.beer
                val calendarWithBeer = CalendarWithBeer(
                        it.id!!,
                        it.name, it.year, it.published,
                        it.archived,
                        it.beerCalendars.stream().map<BeerCalendar> { entity: BeerCalendarEntity? -> beerCalendarMapper.entityToModel(entity!!) }
                                .collect(Collectors.toSet()), calendarTokenMapper.entityToModel(
                        it.calendarToken
                ),
                        beerMapper.entityToModel(beer), beerCalendar.day)
                calendarWithBeers.add(calendarWithBeer)

            }
        }.toSet()
        return calendarWithBeers
    }

    override fun mapModelToEntity(model: Calendar, entity: CalendarEntity) {
        entity.name = model.name
        entity.archived = model.archived
        entity.published = model.published
        entity.year = model.year
    }

    override fun postGet(model: Calendar): Calendar {
        val email = SecurityContextHolder.getContext().authentication?.principal as String?
        if (email != null) {
            val token = model.calendarToken
            val userEntity = userRepository.findByEmailIgnoreCase(email)
            if (userEntity != null) {
                val calendarTokenEntity = userEntity.calendarToken.firstOrNull(CalendarTokenEntity::active)
                if (calendarTokenEntity?.id == token.id) {
                    return model
                }
            }
        }
        return super.postGet(model)
    }
}