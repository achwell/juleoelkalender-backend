package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.entity.BeerStyleEntity
import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.mappers.BeerMapper
import no.juleoelkalender.mappers.CalendarMapper
import no.juleoelkalender.mappers.ReviewMapper
import no.juleoelkalender.mappers.UserWithoutChildrenMapper
import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.BeerWithCalendarAndDay
import no.juleoelkalender.model.BeerWithCalendarDayAndReview
import no.juleoelkalender.repository.BeerCalendarRepository
import no.juleoelkalender.repository.BeerRepository
import no.juleoelkalender.repository.BeerStyleRepository
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.BeerService
import no.juleoelkalender.service.LocalesService
import no.juleoelkalender.utils.ExcelGenerator
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.ZonedDateTime
import java.util.Objects
import java.util.UUID

@Service
class BeerServiceImpl(
    private val beerRepository: BeerRepository,
    private val beerCalendarRepository: BeerCalendarRepository,
    private val beerStyleRepository: BeerStyleRepository,
    private val calendarRepository: CalendarRepository,
    private val userRepository: UserRepository,
    mapper: BeerMapper,
    private val calendarMapper: CalendarMapper,
    private val reviewMapper: ReviewMapper,
    private val userWithoutChildrenMapper: UserWithoutChildrenMapper,
    private val excelGenerator: ExcelGenerator,
    private val localesService: LocalesService
) : BaseServiceImpl<UUID, Beer, BeerEntity>(beerRepository, mapper),
    BeerService {

    override fun getBeersWithReviewByCalendarAndUser(calendarId: UUID, userId: UUID?): Set<BeerWithCalendarDayAndReview> {
        val calendarEntity = calendarRepository.getReferenceById(calendarId)
        val calendar = calendarMapper.entityToModel(calendarEntity)
        val reviewsByBeer = calendarEntity.reviews
            .asSequence()
            .filter { userId == null || it.user.id == userId }
            .map(reviewMapper::entityToModel)
            .toSet()
            .groupBy { it.beer.id }

        return calendarEntity.beerCalendars.map {
            val beer = mapper.entityToModel(it.beer)
            val brewer = userWithoutChildrenMapper.entityToModel(it.beer.user)
            BeerWithCalendarDayAndReview(beer, calendar, brewer, it.day, reviewsByBeer[beer.id]?.firstOrNull())
        }.sortedBy { it.day }.toSet()
    }

    override fun getBeersWithCalendar(calendarId: UUID?, email: String?): Set<BeerWithCalendarAndDay> {
        val currentUserEmail = SecurityContextHolder.getContext().authentication?.principal as String?
        val calendarToken = currentUserEmail?.takeIf(String::isNotBlank)
            ?.let(userRepository::findByEmailIgnoreCase)
            ?.let { user -> getActiveToken(user.calendarToken) ?: return emptySet() }
        val beers = if (!email.isNullOrBlank()) {
            val userEntity = userRepository.findByEmailIgnoreCase(email) ?: return emptySet()
            beerRepository.findBeerEntityByUser(userEntity)
        } else {
            beerRepository.findAll()
        }
        return beers.flatMap { beer ->
            val beerCalendars = beer.beerCalendars.filter { beerCalendar ->
                val calendar = beerCalendar.calendar
                calendarId == null ||
                        (calendar.id == calendarId && (calendarToken == null || calendar.calendarToken.id == calendarToken.id))
            }
            val user = beer.user
            val brewer = userWithoutChildrenMapper.entityToModel(user).let { model ->
                model.copy(calendarToken = model.calendarToken.filter { it.active }.toSet())
            }
            val beerModel = mapper.entityToModel(beer).copy(brewer = brewer)
            if (beerCalendars.isEmpty()) {
                listOf(BeerWithCalendarAndDay(beerModel, null, brewer, 0))
            } else {
                beerCalendars.map { beerCalendar ->
                    BeerWithCalendarAndDay(beerModel, calendarMapper.entityToModel(beerCalendar.calendar), brewer, beerCalendar.day)
                }
            }
        }.toSet()
    }

    override fun getBeersWithCalendarXlsx(calendarId: UUID?, email: String?, locale: String): ByteArray {
        val headers = arrayOf(
            localesService.getString(locale, "pages.beeradmin.beername"),
            localesService.getString(locale, "beer.brewer"),
            localesService.getString(locale, "beer.style"),
            localesService.getString(locale, "beer.abv"),
            localesService.getString(locale, "beer.archived"),
            localesService.getString(locale, "pages.beeradmin.year"),
            localesService.getString(locale, "pages.beeradmin.calendar"),
            localesService.getString(locale, "pages.beeradmin.dayincalendar")
        )
        val rowData = getBeersWithCalendar(calendarId, email).map { beerWithCalendarAndDay ->
            val beer = beerWithCalendarAndDay.beer
            val calendar = beerWithCalendarAndDay.calendar
            arrayOf<Any>(
                beer.name,
                beer.brewer.name(),
                beer.style,
                beer.abv,
                if (beer.archived) localesService.getString(locale, "common.yes") else localesService.getString(locale, "common.no"),
                calendar?.year
                    ?: "",
                calendar?.name
                    ?: "",
                if (calendar != null) beerWithCalendarAndDay.day else ""
            )
        }.toList()
        val sheetname = localesService.getString(locale, "menu.allbeers")
        return excelGenerator.generateReport(sheetname, headers, rowData)
    }

    override fun preCreate(model: Beer): BeerEntity {
        updateBeerStyles(model.style)
        val entity = mapper.modelToEntity(model)
        val email = SecurityContextHolder.getContext().authentication?.principal as String?
        if (email != null) {
            val userEntity = userRepository.findByEmailIgnoreCase(email)
            if (userEntity != null) {
                entity.user = userEntity
            }
        }
        entity.createdDate = ZonedDateTime.now()
        entity.updatedDate = ZonedDateTime.now()
        entity.beerCalendars = mutableSetOf()
        return entity
    }

    @Throws(RuntimeException::class)
    override fun preDelete(id: UUID) {
        if (repository.existsById(id)) {
            repository.findById(id).ifPresent { beerEntity ->
                val authentication = SecurityContextHolder.getContext().authentication
                val authorities = Objects.requireNonNull<Authentication>(authentication).authorities
                val principal = authentication!!.principal
                if (authorities.isEmpty() || authorities.none { "beer:delete_other" == it.authority } && (principal != beerEntity.user.email)) {
                    throw InsufficientAuthenticationException("Ikke lov å slette andres øl")
                }
            }
        }
    }

    override fun preUpdate(model: Beer) {
        updateBeerStyles(model.style)
    }

    override fun getBeersWithCalendarAndReviewByDate(date: LocalDate): Set<BeerWithCalendarDayAndReview> {
        if (date.month != Month.DECEMBER) return emptySet()

        val email = SecurityContextHolder.getContext().authentication?.principal as? String? ?: return emptySet()
        val user = userRepository.findByEmailIgnoreCase(email) ?: return emptySet()
        val activeToken = getActiveToken(user.calendarToken) ?: return emptySet()
        val beerIds = user.beers.map(BeerEntity::id).toSet()

        return calendarRepository.findByCalendarToken(activeToken)
            .asSequence()
            .filter { calendar ->
                calendar.year == date.year && calendar.beerCalendars.any { it.beer.id in beerIds }
            }
            .mapNotNull { calendar ->
                calendar.beerCalendars.firstOrNull { it.day == date.dayOfMonth }?.let { beerCalendar ->
                    val review = beerCalendar.beer.reviews.firstOrNull {
                        it.user.id == user.id && it.calendar.id == calendar.id
                    }?.let(reviewMapper::entityToModel)
                    BeerWithCalendarDayAndReview(
                        mapper.entityToModel(beerCalendar.beer),
                        calendarMapper.entityToModel(calendar),
                        userWithoutChildrenMapper.entityToModel(beerCalendar.beer.user),
                        beerCalendar.day,
                        review
                    )
                }
            }
            .toSet()
    }

    override fun countActiveBeers(): Long = beerRepository.countByArchivedFalse()

    override fun getNewestActiveBeer(): Beer? = beerRepository.findFirstByArchivedFalseOrderByCreatedDateDesc()
        ?.let(mapper::entityToModel)

    override fun countPlacedBeersForYear(year: Int): Long = beerCalendarRepository.countActiveBeerPlacementsForYear(year)

    private fun getActiveToken(calendarToken: Set<CalendarTokenEntity>): CalendarTokenEntity? {
        return calendarToken.firstOrNull(CalendarTokenEntity::active)
    }

    override fun mapModelToEntity(model: Beer, entity: BeerEntity) {
        entity.archived = model.archived
        entity.untapped = model.untapped
        entity.recipe = model.recipe
        entity.ebc = model.ebc
        entity.ibu = model.ibu
        entity.abv = model.abv
        entity.description = model.description
        entity.style = model.style
        entity.name = model.name
        entity.brewedDate = model.brewedDate
        entity.bottleDate = model.bottleDate
        entity.desiredDate = model.desiredDate
    }

    private fun updateBeerStyles(style: String) {
        val beerStyle = beerStyleRepository.findBeerStyleEntitiesByNameIgnoreCase(style)
        if (beerStyle == null) {
            beerStyleRepository.save(BeerStyleEntity(null, style))
        }
    }
}