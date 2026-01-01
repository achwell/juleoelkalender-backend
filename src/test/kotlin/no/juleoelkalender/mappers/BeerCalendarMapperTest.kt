package no.juleoelkalender.mappers

import no.juleoelkalender.entity.BeerCalendarEntity
import no.juleoelkalender.getBeerCalendar
import no.juleoelkalender.getBeerCalendarEntity
import no.juleoelkalender.model.BeerCalendar
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BeerCalendarMapperTest {
    private lateinit var testSubject: BeerCalendarMapper
    private lateinit var beerCalendar: BeerCalendar
    private lateinit var beerCalendarEntity: BeerCalendarEntity

    @BeforeEach
    fun setUp() {
        val calendarTokenMapper = CalendarTokenMapper()
        beerCalendar = getBeerCalendar()
        beerCalendarEntity = getBeerCalendarEntity()
        val beerMapper = BeerMapper(UserWithoutChildrenMapper(calendarTokenMapper, RoleMapper(AuthorityMapper())))
        testSubject = BeerCalendarMapper(beerMapper, CalendarMapper(beerMapper, calendarTokenMapper))
    }

    @Test
    fun testEntityToModel() {
        val beerCalendar = testSubject.entityToModel(beerCalendarEntity)
        assertAll(
                { assertNotNull(beerCalendar) },
                { assertEquals(beerCalendarEntity.beer.id, beerCalendar.beer.id) },
                { assertEquals(beerCalendarEntity.calendar.id, beerCalendar.calendar.id) }
        )
    }

    @Test
    fun testModelToEntity() {
        val beerCalendarEntity = testSubject.modelToEntity(beerCalendar)
        assertAll(
                { assertNotNull(beerCalendarEntity) },
                { assertEquals(beerCalendar.beer.id, beerCalendarEntity.beer.id) },
                {
                    assertEquals(
                            beerCalendar.calendar.id, beerCalendarEntity.calendar.id
                    )
                }
        )
    }
}
