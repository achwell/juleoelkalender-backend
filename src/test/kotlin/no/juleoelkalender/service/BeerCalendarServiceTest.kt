package no.juleoelkalender.service

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.entity.BeerCalendarEntity
import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.getBeerCalendarEntity
import no.juleoelkalender.getBeerEntity
import no.juleoelkalender.getCalendarEntity
import no.juleoelkalender.mappers.*
import no.juleoelkalender.model.BeerCalendar
import no.juleoelkalender.model.Direction
import no.juleoelkalender.repository.BeerCalendarRepository
import no.juleoelkalender.repository.BeerRepository
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.service.impl.BeerCalendarServiceImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.Optional
import java.util.UUID

internal class BeerCalendarServiceTest {
    private val calendarTokenMapper = CalendarTokenMapper()
    val roleMapper = RoleMapper(AuthorityMapper())
    val userWithoutChildrenMapper = UserWithoutChildrenMapper(calendarTokenMapper, roleMapper)
    val beerMapper = BeerMapper(userWithoutChildrenMapper)
    private val beerCalendarMapper = BeerCalendarMapper(beerMapper, CalendarMapper(beerMapper, calendarTokenMapper))

    private val beerCalendarRepository = mockk<BeerCalendarRepository>()
    private val beerRepository = mockk<BeerRepository>()
    private val calendarRepository = mockk<CalendarRepository>()

    private lateinit var testSubject: BeerCalendarService
    private lateinit var beerEntity: BeerEntity
    private lateinit var beerCalendarEntity: BeerCalendarEntity
    private lateinit var calendarEntity: CalendarEntity

    @BeforeEach
    fun setUp() {
        beerEntity = getBeerEntity()
        beerCalendarEntity = getBeerCalendarEntity()
        calendarEntity = getCalendarEntity()
        testSubject = BeerCalendarServiceImpl(beerCalendarRepository, beerRepository, calendarRepository, beerCalendarMapper)
    }

    @Test
    fun testGetBeerCalendars() {
        every { beerCalendarRepository.findAll() } returns listOf(beerCalendarEntity, beerCalendarEntity, beerCalendarEntity)
        val beerCalendars = testSubject.all
        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertEquals(3, beerCalendars.size) }
        )
    }

    @Test
    fun testGetBeerCalendarById() {
        every { beerCalendarRepository.findById(any()) } returns Optional.of(beerCalendarEntity)
        val beerCalendar = testSubject.getById(UUID.randomUUID())
        Assertions.assertNotNull(beerCalendar)
    }

    @Test
    fun testCreateBeerCalendar() {
        every { beerCalendarRepository.getReferenceById(any()) } returns beerCalendarEntity
        every { calendarRepository.getReferenceById(any()) } returns calendarEntity
        every { beerCalendarRepository.save(any()) } returns beerCalendarEntity
        val newBeerCalendar = testSubject.create(
                beerCalendarMapper.entityToModel(
                        beerCalendarEntity
                )
        )

        Assertions.assertAll(
                { Assertions.assertNotNull(newBeerCalendar) },
                { Assertions.assertEquals(calendarEntity.id, newBeerCalendar.calendar.id) },
                { Assertions.assertEquals(beerEntity.id, newBeerCalendar.beer.id) },
                { Assertions.assertEquals(beerCalendarEntity.day, newBeerCalendar.day) }
        )
    }

    @Test
    fun testUpdateBeerCalendar() {
        val bc = beerCalendarMapper.entityToModel(beerCalendarEntity)
        val beerCalendar = BeerCalendar(bc.id, 2, bc.beer, bc.calendar)
        every { beerCalendarRepository.findById(any()) } returns Optional.of(beerCalendarEntity)
        every { beerRepository.getReferenceById(any()) } returns beerEntity
        every { calendarRepository.getReferenceById(any()) } returns calendarEntity
        every { beerCalendarRepository.save(any()) } returns beerCalendarEntity
        val updatedBeerCalendar = testSubject.update(bc.id, beerCalendar)

        Assertions.assertAll(
                { Assertions.assertNotNull(updatedBeerCalendar) },
                { Assertions.assertEquals(calendarEntity.id, updatedBeerCalendar?.calendar?.id) },
                { Assertions.assertEquals(beerEntity.id, updatedBeerCalendar?.beer?.id) },
                { Assertions.assertEquals(beerCalendar.day, updatedBeerCalendar?.day) }
        )
    }

    @Test
    fun testDeleteBeerCalendarExist() {
        every { beerCalendarRepository.existsById(any()) } returns true
        val deleted = testSubject.delete(beerCalendarEntity.id)
        Assertions.assertTrue(deleted)
    }

    @Test
    fun testDeleteBeerCalendarDontExist() {
        every { beerCalendarRepository.existsById(any()) } returns false
        val deleted = testSubject.delete(beerCalendarEntity.id)
        Assertions.assertFalse(deleted)
    }

    @Test
    fun testMoveBeerCalendarUp() {
        val day = 12
        val beerCalendarEntity1 = BeerCalendarEntity(
                beerCalendarEntity.id, day,
                beerEntity, calendarEntity
        )
        val beerCalendarEntity2 = BeerCalendarEntity(
                beerCalendarEntity1.id, day - 1,
                beerEntity, calendarEntity
        )
        every { beerCalendarRepository.findByDayAndCalendar_Id(day, any()) } returns beerCalendarEntity1
        every { beerCalendarRepository.findByDayAndCalendar_Id(beerCalendarEntity2.day, any()) } returns beerCalendarEntity2
        every { beerCalendarRepository.save(beerCalendarEntity1) } returns beerCalendarEntity2
        every { beerCalendarRepository.save(beerCalendarEntity2) } returns beerCalendarEntity1
        try {
            testSubject.moveBeerCalendar(beerCalendarEntity.id, day, Direction.UP)
        } catch (e: Exception) {
            fail("Should not get exception, but got ${e.javaClass.name}")
        }
    }

    @Test
    fun testMoveBeerCalendarDown() {
        val day = 12
        val beerCalendarEntity1 = BeerCalendarEntity(
                beerCalendarEntity.id, day,
                beerEntity, calendarEntity
        )
        val beerCalendarEntity2 = BeerCalendarEntity(
                beerCalendarEntity1.id, day + 1,
                beerEntity, calendarEntity
        )
        every { beerCalendarRepository.findByDayAndCalendar_Id(day, any()) } returns beerCalendarEntity1
        every { beerCalendarRepository.findByDayAndCalendar_Id(beerCalendarEntity2.day, any()) } returns beerCalendarEntity2
        every { beerCalendarRepository.save(beerCalendarEntity1) } returns beerCalendarEntity2
        every { beerCalendarRepository.save(beerCalendarEntity2) } returns beerCalendarEntity1
        try {
            testSubject.moveBeerCalendar(beerCalendarEntity.id, day, Direction.DOWN)
        } catch (e: Exception) {
            fail("Should not get exception, but got ${e.javaClass.name}")
        }
    }
}