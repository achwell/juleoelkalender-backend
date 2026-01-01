package no.juleoelkalender.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.juleoelkalender.*
import no.juleoelkalender.entity.*
import no.juleoelkalender.mappers.*
import no.juleoelkalender.model.Beer
import no.juleoelkalender.repository.BeerRepository
import no.juleoelkalender.repository.BeerStyleRepository
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.impl.BeerServiceImpl
import no.juleoelkalender.utils.ExcelGenerator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate
import java.time.Month
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID

internal class BeerServiceTest {
    private val calendarTokenMapper = CalendarTokenMapper()
    private val userWithoutChildrenMapper = UserWithoutChildrenMapper(calendarTokenMapper, RoleMapper(AuthorityMapper()))
    private val beerMapper = BeerMapper(userWithoutChildrenMapper)
    private val calendarMapper = CalendarMapper(beerMapper, calendarTokenMapper)
    private val reviewMapper = ReviewMapper(beerMapper, calendarMapper, userWithoutChildrenMapper)

    private val beerRepository = mockk<BeerRepository>()
    private val beerStyleRepository = mockk<BeerStyleRepository>()
    private val calendarRepository = mockk<CalendarRepository>()
    private val userRepository = mockk<UserRepository>()
    private val localesService = mockk<LocalesService>()

    private lateinit var testSubject: BeerService
    private lateinit var beer: Beer
    private lateinit var beerEntity: BeerEntity
    private lateinit var beerStyleEntity: BeerStyleEntity
    private lateinit var calendarEntity: CalendarEntity
    private lateinit var userEntity: UserEntity

    @BeforeEach
    fun setUp() {
        beer = getBeer()
        beerEntity = getBeerEntity()
        beerStyleEntity = getBeerStyleEntity()
        calendarEntity = getCalendarEntity()
        userEntity = getUserEntity()
        testSubject = BeerServiceImpl(
                beerRepository,
                beerStyleRepository,
                calendarRepository,
                userRepository,
                beerMapper,
                calendarMapper,
                reviewMapper,
                userWithoutChildrenMapper,
                ExcelGenerator(),
                localesService
        )
    }

    @Test
    fun testGetBeers() {
        every { beerRepository.findAll() } returns listOf(beerEntity, beerEntity, beerEntity)
        val beers = testSubject.all
        Assertions.assertAll(
                { Assertions.assertNotNull(beers) },
                { Assertions.assertEquals(3, beers.size) }
        )
    }

    @Test
    fun getBeersWithReviewByCalendarAndUser() {
        every { calendarRepository.getReferenceById(any()) } returns calendarEntity
        val beers = testSubject.getBeersWithReviewByCalendarAndUser(
                calendarEntity.id!!, userEntity.id
        )
        Assertions.assertAll(
                { Assertions.assertNotNull(beers) },
                { Assertions.assertEquals(1, beers.size) },
                { Assertions.assertEquals(1, beers.first().day) },
                { Assertions.assertNotNull(beers.first().review) },
                { Assertions.assertNotNull(beers.first().calendar) },
                { Assertions.assertNotNull(beers.first().beer) }
        )
    }

    @Test
    fun testGetBeersWithCalendarNoUser() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { beerRepository.findAll() } returns listOf(beerEntity)

        val beers = testSubject.getBeersWithCalendar(calendarEntity.id, null)

        Assertions.assertAll(
                { Assertions.assertNotNull(beers) },
                { Assertions.assertEquals(1, beers.size) },
                { Assertions.assertEquals(1, beers.first().day) },
                { Assertions.assertNotNull(beers.first().calendar) },
                { Assertions.assertNotNull(beers.first().brewer) }
        )
    }

    @Test
    fun testGetBeersWithCalendarWithUser() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { beerRepository.findAll() } returns listOf(beerEntity)
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { beerRepository.findBeerEntityByUser(any()) } returns listOf(beerEntity)

        val beers = testSubject.getBeersWithCalendar(
                calendarEntity.id,
                userEntity.email
        )

        Assertions.assertAll(
                { Assertions.assertNotNull(beers) },
                { Assertions.assertEquals(1, beers.size) },
                { Assertions.assertEquals(1, beers.first().day) },
                { Assertions.assertNotNull(beers.first().calendar) },
                { Assertions.assertNotNull(beers.first().brewer) }
        )
    }

    @Test
    fun testGetBeerById() {
        every { beerRepository.findById(any()) } returns Optional.of(beerEntity)
        val beer = testSubject.getById(beerEntity.id)

        Assertions.assertAll(
                { Assertions.assertNotNull(beer) },
                { Assertions.assertEquals(beerEntity.id, beer?.id) }
        )
    }

    @Test
    fun testCreateBeer() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { beerStyleRepository.findBeerStyleEntitiesByNameIgnoreCase(any()) } returns beerStyleEntity
        every { beerRepository.save(any()) } returns beerEntity
        val newBeer = testSubject.create(beer)
        Assertions.assertEquals(beer.id, newBeer.id)
    }

    @Test
    fun testCreateBeerNewStyle() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { beerStyleRepository.findBeerStyleEntitiesByNameIgnoreCase(any()) } returns null
        every { beerStyleRepository.save(any()) } returns beerStyleEntity
        every { beerRepository.save(any()) } returns beerEntity

        val newBeer = testSubject.create(beer)
        Assertions.assertEquals(beer.id, newBeer.id)
    }

    @Test
    fun testUpdateBeer() {
        every { beerStyleRepository.findBeerStyleEntitiesByNameIgnoreCase(any()) } returns beerStyleEntity
        every { beerRepository.findById(any()) } returns Optional.of(beerEntity)
        every { beerRepository.save(any()) } returns beerEntity
        val updatedBeer = testSubject.update(beer.id, beer)
        Assertions.assertEquals(beer.id, updatedBeer?.id)
    }

    @Test
    fun testDeleteBeerExist() {
        every { beerRepository.existsById(any()) } returns true
        every { beerRepository.deleteById(any()) } just Runs
        val deleted = testSubject.delete(beerEntity.id)
        Assertions.assertTrue(deleted)
    }

    @Test
    fun testDeleteBeerDontExist() {
        every { beerRepository.existsById(any()) } returns false
        val deleted = testSubject.delete(beerEntity.id)
        Assertions.assertFalse(deleted)
    }

    @Test
    fun testDeleteBeerWrongOwner() {
        every { beerRepository.existsById(any()) } returns true
        every { beerRepository.findById(any()) } returns Optional.of(beerEntity)
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication } returns authentication
        every { authentication.authorities } returns listOf()
        every { authentication.principal } returns userEntity.email

        every { beerStyleRepository.findBeerStyleEntitiesByNameIgnoreCase(any()) } returns null
        every { beerStyleRepository.save(any()) } returns beerStyleEntity

        val thrown = assertThrows<InsufficientAuthenticationException> { testSubject.delete(beerEntity.id) }
        Assertions.assertAll(
                { Assertions.assertNotNull(thrown) },
                { Assertions.assertEquals("Ikke lov å slette andres øl", thrown.message) }
        )
    }

    @Test
    fun testGetTodaysBeersWithCalendarAndReview() {
        val now = ZonedDateTime.now()
        val review = ReviewEntity(UUID.randomUUID(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", now, now, beerEntity, calendarEntity, userEntity)
        val beer1 = beerEntity.apply { reviews = mutableSetOf(review) }
        val beerCalendar = BeerCalendarEntity(UUID.randomUUID(), 1, beer1, calendarEntity.apply { year = 2023 })
        val calendarEntity2 = calendarEntity.apply { beerCalendars = mutableSetOf(beerCalendar) }

        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity.apply { beers = mutableSetOf(beerMapper.modelToEntity(beer)) }
        every { calendarRepository.findByCalendarToken(any()) } returns listOf(calendarEntity2)

        val beers = testSubject.getBeersWithCalendarAndReviewByDate(LocalDate.of(2023, Month.DECEMBER, 1))

        Assertions.assertAll(
                { Assertions.assertNotNull(beers) },
                { Assertions.assertEquals(1, beers.size) },
                { Assertions.assertEquals(1, beers.first().day) },
                { Assertions.assertNotNull(beers.first().calendar) },
                { Assertions.assertNotNull(beers.first().brewer) }
        )
    }

    @Test
    fun testGetTodaysBeersWithCalendarAndReviewNovember() {
        val beers = testSubject.getBeersWithCalendarAndReviewByDate(
                LocalDate.of(
                        calendarEntity.year, Month.NOVEMBER, 1
                )
        )
        Assertions.assertAll(
                { Assertions.assertNotNull(beers) },
                { Assertions.assertEquals(0, beers.size) }
        )
    }

    @Test
    fun testGetBeersWithCalendarXlsx() {
        val locale = "no"

        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { localesService.getString(locale, "pages.beeradmin.beername") } returns "beername"
        every { localesService.getString(locale, "beer.brewer") } returns "brewer"
        every { localesService.getString(locale, "beer.style") } returns "style"
        every { localesService.getString(locale, "beer.abv") } returns "abv"
        every { localesService.getString(locale, "beer.archived") } returns "archived"
        every { localesService.getString(locale, "pages.beeradmin.year") } returns "year"
        every { localesService.getString(locale, "pages.beeradmin.calendar") } returns "calendar"
        every { localesService.getString(locale, "pages.beeradmin.dayincalendar") } returns "dayincalendar"
        every { localesService.getString(locale, "menu.allbeers") } returns "allbeers"
        every { localesService.getString(locale, "common.yes") } returns "yes"
        every { beerRepository.findAll() } returns listOf(beerEntity)
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { beerRepository.findBeerEntityByUser(any()) } returns listOf(beerEntity)

        val xlsx = testSubject.getBeersWithCalendarXlsx(calendarEntity.id, userEntity.email, locale)
        Assertions.assertNotNull(xlsx)
    }

    @Test
    fun testGetBeersWithCalendarXlsxBeerWithoutCalendar() {
        val locale = "no"
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { localesService.getString(locale, "pages.beeradmin.beername") } returns "beername"
        every { localesService.getString(locale, "beer.brewer") } returns "brewer"
        every { localesService.getString(locale, "beer.style") } returns "style"
        every { localesService.getString(locale, "beer.abv") } returns "abv"
        every { localesService.getString(locale, "beer.archived") } returns "archived"
        every { localesService.getString(locale, "pages.beeradmin.year") } returns "year"
        every { localesService.getString(locale, "pages.beeradmin.calendar") } returns "calendar"
        every { localesService.getString(locale, "pages.beeradmin.dayincalendar") } returns "dayincalendar"
        every { localesService.getString(locale, "menu.allbeers") } returns "allbeers"
        every { localesService.getString(locale, "common.yes") } returns "yes"
        every { beerRepository.findAll() } returns listOf(beerEntity)
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { beerRepository.findBeerEntityByUser(any()) } returns listOf(beerEntity)

        val xlsx = testSubject.getBeersWithCalendarXlsx(calendarEntity.id, userEntity.email, locale)
        Assertions.assertNotNull(xlsx)
    }
}