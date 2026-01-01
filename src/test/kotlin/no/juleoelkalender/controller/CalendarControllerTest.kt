package no.juleoelkalender.controller

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.getCalendar
import no.juleoelkalender.getCalendarWithBeer
import no.juleoelkalender.model.Calendar
import no.juleoelkalender.model.CalendarWithBeer
import no.juleoelkalender.service.CalendarService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatusCode
import java.net.URISyntaxException

internal class CalendarControllerTest {

    private val calendarService = mockk<CalendarService>()

    private lateinit var testSubject: CalendarController
    private lateinit var calendar: Calendar
    private lateinit var calendarWithBeer: CalendarWithBeer

    @BeforeEach
    fun setUp() {
        calendar = getCalendar()
        calendarWithBeer = getCalendarWithBeer()
        testSubject = CalendarController(calendarService)
    }

    @Test
    fun testGetCalendars() {
        every { calendarService.all } returns setOf(calendar)
        val calendars = testSubject.calendars

        Assertions.assertAll(
                { Assertions.assertNotNull(calendars) },
                { Assertions.assertNotNull(calendars.statusCode) },
                { Assertions.assertNotNull(calendars.getBody()) },
                { Assertions.assertEquals(1, calendars.getBody()?.size) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), calendars.statusCode) }
        )
    }

    @Test
    fun testGetCalendarById() {
        every { calendarService.getById(any()) } returns calendar
        val calendar = testSubject.getCalendarById(calendar.id!!)

        Assertions.assertAll(
                { Assertions.assertNotNull(calendar) },
                { Assertions.assertNotNull(calendar.statusCode) },
                { Assertions.assertNotNull(calendar.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), calendar.statusCode) }
        )
    }

    @Test
    fun testGetCalendarWithBeers() {
        every { calendarService.getCalendarWithBeers(any()) } returns setOf(calendarWithBeer)
        val calendars = testSubject.getCalendarWithBeers(calendar.id!!)

        Assertions.assertAll(
                { Assertions.assertNotNull(calendars) },
                { Assertions.assertNotNull(calendars.statusCode) },
                { Assertions.assertNotNull(calendars.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), calendars.statusCode) }
        )
    }

    @Test
    @Throws(URISyntaxException::class)
    fun testCreateCalendar() {
        every { calendarService.create(any()) } returns calendar
        val calendar = testSubject.createCalendar(this.calendar)

        Assertions.assertAll(
                { Assertions.assertNotNull(calendar) },
                { Assertions.assertNotNull(calendar.statusCode) },
                { Assertions.assertNotNull(calendar.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(201), calendar.statusCode) }
        )
    }

    @Test
    fun testUpdateCalendar() {
        every { calendarService.update(any(), any()) } returns calendar
        val calendar = testSubject.updateCalendar(
                calendar.id!!,
                calendar
        )

        Assertions.assertAll(
                { Assertions.assertNotNull(calendar) },
                { Assertions.assertNotNull(calendar.statusCode) },
                { Assertions.assertNotNull(calendar.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), calendar.statusCode) }
        )
    }

    @Test
    fun deleteCalendar() {
        every { calendarService.delete(any()) } returns true
        val calendar = testSubject.deleteCalendar(calendar.id!!)

        Assertions.assertAll(
                { Assertions.assertNotNull(calendar) },
                { Assertions.assertNotNull(calendar.statusCode) },
                { Assertions.assertNull(calendar.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(204), calendar.statusCode) }
        )
    }
}