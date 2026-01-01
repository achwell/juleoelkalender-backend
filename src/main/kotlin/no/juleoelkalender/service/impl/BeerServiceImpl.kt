package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.entity.BeerStyleEntity
import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.mappers.BeerMapper
import no.juleoelkalender.mappers.CalendarMapper
import no.juleoelkalender.mappers.ReviewMapper
import no.juleoelkalender.mappers.UserWithoutChildrenMapper
import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.BeerDay
import no.juleoelkalender.model.BeerWithCalendarAndDay
import no.juleoelkalender.model.BeerWithCalendarDayAndReview
import no.juleoelkalender.repository.BeerRepository
import no.juleoelkalender.repository.BeerStyleRepository
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.BeerService
import no.juleoelkalender.service.LocalesService
import no.juleoelkalender.utils.ExcelGenerator
import org.apache.commons.lang3.StringUtils
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.ZonedDateTime
import java.util.Objects
import java.util.UUID
import java.util.function.Consumer

@Service
class BeerServiceImpl(private val beerRepository: BeerRepository, private val beerStyleRepository: BeerStyleRepository, private val calendarRepository: CalendarRepository, private val userRepository: UserRepository, mapper: BeerMapper, private val calendarMapper: CalendarMapper, private val reviewMapper: ReviewMapper, private val userWithoutChildrenMapper: UserWithoutChildrenMapper, private val excelGenerator: ExcelGenerator, private val localesService: LocalesService) : BaseServiceImpl<UUID, Beer, BeerEntity>(beerRepository, mapper),
        BeerService {

    override fun getBeersWithReviewByCalendarAndUser(calendarId: UUID, userId: UUID?): Set<BeerWithCalendarDayAndReview> {
        val calendarEntity = calendarRepository.getReferenceById(calendarId)
        val calendar = calendarMapper.entityToModel(calendarEntity)
        val allReviews = calendarEntity.reviews.map { reviewMapper.entityToModel(it) }.toSet()

        return calendarEntity.beerCalendars.map {
            val beer = mapper.entityToModel(it.beer)
            val user = userWithoutChildrenMapper.entityToModel(it.beer.user)
            val day = it.day
            BeerDay(beer, user, day)
        }.map {
            val beer = it.beer
            val brewer = it.brewer
            val day = it.day
            val review = allReviews.firstOrNull { r ->
                if (userId == null) (r.beer.id == beer.id)
                else (r.user.id == userId && r.beer.id == beer.id)
            }
            BeerWithCalendarDayAndReview(beer, calendar, brewer, day, review)
        }.sortedBy { it.day }.toSet()
    }

    override fun getBeersWithCalendar(calendarId: UUID?, email: String?): Set<BeerWithCalendarAndDay> {
        val currentUserEmail = SecurityContextHolder.getContext().authentication?.principal as String?
        val retVal: MutableSet<BeerWithCalendarAndDay> = mutableSetOf()
        var beers = repository.findAll()
        val calendarToken: CalendarTokenEntity?
        if (StringUtils.isNotBlank(currentUserEmail)) {
            val userEntity = userRepository.findByEmailIgnoreCase(currentUserEmail!!)
            if (userEntity != null) {
                calendarToken = userEntity.calendarToken.stream().filter(CalendarTokenEntity::active).findFirst().orElse(null)
                if (calendarToken == null) {
                    return mutableSetOf()
                }
            } else {
                calendarToken = null
            }
        } else {
            calendarToken = null
        }
        if (StringUtils.isNotBlank(email)) {
            val userEntity = userRepository.findByEmailIgnoreCase(email!!)
            if (userEntity != null) {
                beers = beerRepository.findBeerEntityByUser(userEntity).toMutableList()
            }
        }
        beers.forEach(Consumer { beer ->
            val beerCalendars = beer.beerCalendars.filter { beerCalendar ->
                if (calendarId == null) {
                    return@filter true
                }
                val calendar = beerCalendar.calendar
                if (calendarToken != null && calendar.calendarToken.id != calendarToken.id) {
                    return@filter false
                }
                calendar.id == calendarId
            }.toSet()
            val user = beer.user
            user.calendarToken = user.calendarToken.filter(CalendarTokenEntity::active).toMutableSet()
            if (beerCalendars.isEmpty()) {
                retVal.add(BeerWithCalendarAndDay(mapper.entityToModel(beer), null, userWithoutChildrenMapper.entityToModel(user), 0))
            } else {
                beerCalendars.forEach(Consumer { beerCalendar ->
                    retVal.add(BeerWithCalendarAndDay(mapper.entityToModel(beer), calendarMapper.entityToModel(beerCalendar.calendar), userWithoutChildrenMapper.entityToModel(user), beerCalendar.day))
                })
            }
        })
        return retVal
    }

    override fun getBeersWithCalendarXlsx(calendarId: UUID?, email: String?, locale: String): ByteArray {
        val headers = arrayOf(localesService.getString(locale, "pages.beeradmin.beername"), localesService.getString(locale, "beer.brewer"), localesService.getString(locale, "beer.style"), localesService.getString(locale, "beer.abv"), localesService.getString(locale, "beer.archived"), localesService.getString(locale, "pages.beeradmin.year"), localesService.getString(locale, "pages.beeradmin.calendar"), localesService.getString(locale, "pages.beeradmin.dayincalendar"))
        val rowData = getBeersWithCalendar(calendarId, email).map { beerWithCalendarAndDay ->
            val beer = beerWithCalendarAndDay.beer
            val calendar = beerWithCalendarAndDay.calendar
            arrayOf<Any>(beer.name, beer.brewer.name(), beer.style, beer.abv, if (beer.archived) localesService.getString(locale, "common.no") else localesService.getString(locale, "common.yes"), calendar?.year
                    ?: "", calendar?.name
                    ?: "", if (calendar != null) beerWithCalendarAndDay.day else "")
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
            repository.findById(id).ifPresent(Consumer { beerEntity: BeerEntity? ->
                val authentication = SecurityContextHolder.getContext().authentication
                val authorities = Objects.requireNonNull<Authentication>(authentication).authorities
                val principal = authentication!!.principal
                if (authorities.isEmpty() || authorities.none { "beer:delete_other" == it.authority } && (principal != beerEntity!!.user.email)) {
                    throw InsufficientAuthenticationException("Ikke lov å slette andres øl")
                }
            })
        }
    }

    override fun preUpdate(model: Beer) {
        updateBeerStyles(model.style)
    }

    override fun getBeersWithCalendarAndReviewByDate(date: LocalDate): Set<BeerWithCalendarDayAndReview> {
        val month = date.month
        val beers: MutableSet<BeerWithCalendarDayAndReview> = mutableSetOf()
        if (month == Month.DECEMBER) {
            val email = SecurityContextHolder.getContext().authentication?.principal as String?
            if (email != null) {
                val user = userRepository.findByEmailIgnoreCase(email)
                if (user != null) {
                    val beerIds = user.beers.map(BeerEntity::id).toSet()
                    val activeToken = getActiveToken(user.calendarToken)
                    if (activeToken != null) {
                        val year = date.year
                        val day = date.dayOfMonth
                        calendarRepository.findByCalendarToken(activeToken).filter { it.year == year }.forEach { calendarEntity ->
                            val beerCalendars = calendarEntity.beerCalendars
                            if (beerCalendars.any { bc -> beerIds.contains(bc.beer.id) }) {
                                val beerCalendar = beerCalendars.firstOrNull { it.day == day }
                                if (beerCalendar != null) {
                                    val reviewEntity = beerCalendar.beer.reviews.filter { it.user.id === user.id }.map { reviewMapper.entityToModel(it) }.first()
                                    beers.add(
                                            BeerWithCalendarDayAndReview(
                                                    mapper.entityToModel(beerCalendar.beer),
                                                    calendarMapper.entityToModel(calendarEntity),
                                                    userWithoutChildrenMapper.entityToModel(beerCalendar.beer.user),
                                                    beerCalendar.day,
                                                    reviewEntity
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return beers
    }

    private fun getActiveToken(calendarToken: MutableSet<CalendarTokenEntity>): CalendarTokenEntity? {
        val activeToken = arrayOf<CalendarTokenEntity?>(null)
        calendarToken.forEach(Consumer {
            if (it.active && activeToken[0] == null) {
                activeToken[0] = it
            }
        })
        return activeToken[0]
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