package no.juleoelkalender.mappers

import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.getCalendar
import no.juleoelkalender.getCalendarEntity
import no.juleoelkalender.model.Calendar
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CalendarMapperTest {
    private lateinit var testSubject: CalendarMapper
    private lateinit var calendar: Calendar
    private lateinit var calendarEntity: CalendarEntity

    @BeforeEach
    fun setUp() {
        calendar = getCalendar()
        calendarEntity = getCalendarEntity()
        val calendarTokenMapper = CalendarTokenMapper()
        val beerMapper = BeerMapper(UserWithoutChildrenMapper(calendarTokenMapper, RoleMapper(AuthorityMapper())))
        testSubject = CalendarMapper(beerMapper, calendarTokenMapper)
    }


    @Test
    fun testEntityToModel() {
        val calendar = testSubject.entityToModel(calendarEntity)
        assertAll(
                { assertNotNull(calendar) },
                { assertEquals(calendarEntity.id, calendar.id) },
                { assertEquals(calendarEntity.name, calendar.name) },
                { assertEquals(calendarEntity.year, calendar.year) },
                { assertEquals(calendarEntity.published, calendar.published) }
        )
    }

    @Test
    fun testModelToEntity() {
        val calendarEntity = testSubject.modelToEntity(calendar)
        assertAll(
                { assertNotNull(calendarEntity) },
                { assertEquals(calendar.id, calendarEntity.id) },
                { assertEquals(calendar.name, calendarEntity.name) },
                { assertEquals(calendar.year, calendarEntity.year) },
                { assertEquals(calendar.published, calendarEntity.published) }
        )
    }
}
