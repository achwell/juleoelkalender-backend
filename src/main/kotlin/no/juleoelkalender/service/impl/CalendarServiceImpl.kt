package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.mappers.BeerCalendarMapper
import no.juleoelkalender.mappers.BeerMapper
import no.juleoelkalender.mappers.CalendarMapper
import no.juleoelkalender.mappers.CalendarTokenMapper
import no.juleoelkalender.model.Calendar
import no.juleoelkalender.model.CalendarWithBeer
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.CalendarService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class CalendarServiceImpl(
    private val calendarRepository: CalendarRepository, private val userRepository: UserRepository,
    private val beerMapper: BeerMapper, private val beerCalendarMapper: BeerCalendarMapper, mapper: CalendarMapper,
    private val calendarTokenMapper: CalendarTokenMapper
) : BaseServiceImpl<UUID, Calendar, CalendarEntity>(calendarRepository, mapper), CalendarService {

    override val all: Set<Calendar>
        get() = repository.findAll().map { mapper.entityToModel(it) }.toSet()

    override fun countActiveCalendarsForYear(year: Int): Long =
        calendarRepository.countByArchivedFalseAndYear(year)

    override fun getById(id: UUID): Calendar? {
        return repository.findById(id).map { mapper.entityToModel(it) }.getOrNull()
    }

    override fun getCalendarWithBeers(calendarId: UUID): Set<CalendarWithBeer> {
        val calendar = repository.findById(calendarId).getOrNull() ?: return emptySet()
        val beerCalendars = calendar.beerCalendars.map(beerCalendarMapper::entityToModel).toSet()
        val calendarToken = calendarTokenMapper.entityToModel(calendar.calendarToken)
        return calendar.beerCalendars.map { beerCalendar ->
            CalendarWithBeer(
                calendar.id!!, calendar.name, calendar.year, calendar.published,
                calendar.archived, beerCalendars, calendarToken,
                beerMapper.entityToModel(beerCalendar.beer), beerCalendar.day
            )
        }.toSet()
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