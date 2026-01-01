package no.juleoelkalender.mappers

import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.getCalendarToken
import no.juleoelkalender.getCalendarTokenEntity
import no.juleoelkalender.model.CalendarToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CalendarTokenMapperTest {
    private lateinit var testSubject: CalendarTokenMapper
    private lateinit var calendarTokenEntity: CalendarTokenEntity
    private lateinit var calendarToken: CalendarToken

    @BeforeEach
    fun setUp() {
        testSubject = CalendarTokenMapper()
        calendarTokenEntity = getCalendarTokenEntity()
        calendarToken = getCalendarToken()
    }

    @Test
    fun testEntityToModel() {
        val calendarToken = testSubject.entityToModel(calendarTokenEntity)
        assertAll(
                { assertNotNull(calendarToken) },
                { assertEquals(calendarTokenEntity.name, calendarToken.name) },
                { assertEquals(calendarTokenEntity.id, calendarToken.id) })
    }

    @Test
    fun testModelToEntity() {
        val calendarTokenEntity = testSubject.modelToEntity(calendarToken)
        assertAll(
                { assertNotNull(calendarTokenEntity) },
                { assertEquals(calendarToken.name, calendarTokenEntity.name) },
                { assertEquals(calendarToken.id, calendarTokenEntity.id) })
    }
}
