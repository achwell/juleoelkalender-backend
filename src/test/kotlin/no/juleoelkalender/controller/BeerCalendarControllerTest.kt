package no.juleoelkalender.controller

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.getBeerCalendar
import no.juleoelkalender.model.BeerCalendar
import no.juleoelkalender.model.Direction
import no.juleoelkalender.service.BeerCalendarService
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatusCode
import java.net.URISyntaxException

internal class BeerCalendarControllerTest {
    private val beerCalendarService = mockk<BeerCalendarService>()

    private lateinit var testSubject: BeerCalendarController
    private lateinit var beerCalendar: BeerCalendar

    @BeforeEach
    fun setUp() {
        beerCalendar = getBeerCalendar()
        testSubject = BeerCalendarController(beerCalendarService)
    }

    @Test
    fun testGetBeerCalendars() {
        every { beerCalendarService.all } returns setOf(beerCalendar)
        val beerCalendars = testSubject.beerCalendars

        assertAll(
                { assertNotNull(beerCalendars) },
                { assertNotNull(beerCalendars.statusCode) },
                { assertNotNull(beerCalendars.getBody()) },
                { assertEquals(1, beerCalendars.getBody()?.size) },
                { assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testGetBeerCalendarById() {
        every { beerCalendarService.getById(any()) } returns beerCalendar
        val beerCalendars = testSubject.getBeerCalendarById(beerCalendar.id)

        assertAll(
                { assertNotNull(beerCalendars) },
                { assertNotNull(beerCalendars.statusCode) },
                { assertNotNull(beerCalendars.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testGetBeerCalendarByIdNotFound() {
        every { beerCalendarService.getById(any()) } returns null

        val thrown = assertThrows(
                NotFoundException::class.java
        ) { testSubject.getBeerCalendarById(beerCalendar.id) }
        assertAll(
                { assertNotNull(thrown) },
                { assertNull(thrown.message) }
        )
    }

    @Test
    @Throws(URISyntaxException::class)
    fun testCreateBeerCalendar() {
        every { beerCalendarService.create(any()) } returns beerCalendar
        val beerCalendars = testSubject.createBeerCalendar(
                beerCalendar
        )

        assertAll(
                { assertNotNull(beerCalendars) },
                { assertNotNull(beerCalendars.statusCode) },
                { assertNotNull(beerCalendars.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(201), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testMoveBeerCalendar() {
        every { beerCalendarService.moveBeerCalendar(any(), any(), any()) } just Runs

        val beerCalendars = testSubject.moveBeerCalendar(
                beerCalendar.id, 10,
                Direction.DOWN
        )

        assertAll(
                { assertNotNull(beerCalendars) },
                { assertNotNull(beerCalendars.statusCode) },
                { assertNull(beerCalendars.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(204), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testUpdateBeerCalendar() {
        every { beerCalendarService.update(any(), any()) } returns beerCalendar
        val beerCalendars = testSubject.updateBeerCalendar(
                beerCalendar.id,
                beerCalendar
        )

        assertAll(
                { assertNotNull(beerCalendars) },
                { assertNotNull(beerCalendars.statusCode) },
                { assertNotNull(beerCalendars.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun deleteBeerCalendar() {
        every { beerCalendarService.delete(any()) } returns true
        val beerCalendars = testSubject.deleteBeerCalendar(beerCalendar.id)

        assertAll(
                { assertNotNull(beerCalendars) },
                { assertNotNull(beerCalendars.statusCode) },
                { assertNull(beerCalendars.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(204), beerCalendars.statusCode) }
        )
    }
}
