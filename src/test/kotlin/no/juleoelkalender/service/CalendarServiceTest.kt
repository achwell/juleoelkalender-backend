package no.juleoelkalender.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.juleoelkalender.entity.BeerCalendarEntity
import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.getBeer
import no.juleoelkalender.getBeerCalendarEntity
import no.juleoelkalender.getCalendar
import no.juleoelkalender.getCalendarEntity
import no.juleoelkalender.mappers.*
import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.Calendar
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.impl.CalendarServiceImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

internal class CalendarServiceTest {
    private val calendarTokenMapper = CalendarTokenMapper()
    private val beerMapper = BeerMapper(UserWithoutChildrenMapper(calendarTokenMapper, RoleMapper(AuthorityMapper())))
    private val calendarMapper = CalendarMapper(beerMapper, calendarTokenMapper)
    private val beerCalendarMapper = BeerCalendarMapper(beerMapper, calendarMapper)

    private val calendarRepository = mockk<CalendarRepository>()
    private val userRepository = mockk<UserRepository>()
    private lateinit var testSubject: CalendarService
    private lateinit var beer: Beer
    private lateinit var beerCalendarEntity: BeerCalendarEntity
    private lateinit var calendar: Calendar
    private lateinit var calendarEntity: CalendarEntity

    @BeforeEach
    fun setUp() {
        beer = getBeer()
        beerCalendarEntity = getBeerCalendarEntity()
        calendar = getCalendar()
        calendarEntity = getCalendarEntity()
        testSubject = CalendarServiceImpl(calendarRepository, userRepository, beerMapper, beerCalendarMapper, calendarMapper, calendarTokenMapper)
    }

    @Test
    fun testGetCalendars() {
        every { calendarRepository.findAll() } returns listOf(calendarEntity, calendarEntity, calendarEntity)

        val calendars = testSubject.all
        Assertions.assertAll(
                { Assertions.assertNotNull(calendars) },
                { Assertions.assertEquals(3, calendars.size) }
        )
    }

    @Test
    fun testGetCalendarWithBeers() {
        every { calendarRepository.findById(any()) } returns Optional.of(calendarEntity)

        val calendarWithBeers = testSubject.getCalendarWithBeers(calendarEntity.id!!)
        Assertions.assertAll(
                { Assertions.assertNotNull(calendarWithBeers) },
                { Assertions.assertEquals(1, calendarWithBeers.size) }
        )
        val calendarWithBeer = calendarWithBeers.first()
        Assertions.assertAll(
                { Assertions.assertEquals(beer.id, calendarWithBeer.beer.id) },
                { Assertions.assertEquals(beer.name, calendarWithBeer.beer.name) },
                { Assertions.assertEquals(calendarEntity.name, calendarWithBeer.name) },
                { Assertions.assertEquals(calendarEntity.year, calendarWithBeer.year) },
                { Assertions.assertEquals(beerCalendarEntity.day, calendarWithBeer.day) },
                { Assertions.assertEquals(calendarEntity.published, calendarWithBeer.published) },
                { Assertions.assertEquals(calendarEntity.archived, calendarWithBeer.archived) }
        )
    }

    @Test
    fun testGetCalendarById() {
        every { calendarRepository.findById(any()) } returns Optional.of(calendarEntity)
        val calendar = testSubject.getById(calendarEntity.id!!)
        Assertions.assertAll(
                { Assertions.assertNotNull(calendar) },
                { Assertions.assertEquals(calendarEntity.id, calendar?.id) }
        )
    }

    @Test
    fun testCreateCalendar() {
        every { calendarRepository.save(any()) } returns calendarEntity
        val calendar = testSubject.create(calendarMapper.entityToModel(calendarEntity))
        Assertions.assertAll(
                { Assertions.assertNotNull(calendar) },
                { Assertions.assertEquals(calendarEntity.id, calendar.id) },
                { Assertions.assertEquals(calendarEntity.name, calendar.name) },
                { Assertions.assertEquals(calendarEntity.year, calendar.year) },
                { Assertions.assertFalse(calendar.archived) }
        )
    }

    @Test
    fun testUpdateCalendar() {
        val calendar = Calendar(
                this.calendar.id, "UPDATED NAME",
                this.calendar.year, this.calendar.published, this.calendar.archived,
                this.calendar.beerCalendars, this.calendar.calendarToken
        )

        every { calendarRepository.findById(any()) } returns Optional.of(calendarEntity)
        every { calendarRepository.save(any()) } returns calendarMapper.modelToEntity(calendar)

        val updatedCalendar = testSubject.update(this.calendar.id!!, calendar)
        Assertions.assertAll(
                { Assertions.assertNotNull(updatedCalendar) },
                { Assertions.assertEquals(calendarEntity.id, updatedCalendar?.id) },
                { Assertions.assertEquals(calendar.year, updatedCalendar?.year) },
                { Assertions.assertEquals(calendarEntity.name, updatedCalendar?.name) },
                { Assertions.assertFalse(updatedCalendar?.archived ?: false) }
        )
    }


    @Test
    fun testDeleteCalendarExist() {
        every { calendarRepository.existsById(any()) } returns true
        every { calendarRepository.deleteById(any()) } just Runs
        val deleted = testSubject.delete(calendar.id!!)
        Assertions.assertTrue(deleted)
    }

    @Test
    fun testDeleteCalendarDontExist() {
        every { calendarRepository.existsById(any()) } returns false
        val deleted = testSubject.delete(calendar.id!!)
        Assertions.assertFalse(deleted)
    }
}