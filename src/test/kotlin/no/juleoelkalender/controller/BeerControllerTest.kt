package no.juleoelkalender.controller

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.getBeer
import no.juleoelkalender.getBeerWithCalendarAndDay
import no.juleoelkalender.getBeerWithCalendarDayAndReview
import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.BeerWithCalendarAndDay
import no.juleoelkalender.model.BeerWithCalendarDayAndReview
import no.juleoelkalender.service.BeerService
import no.juleoelkalender.service.LocalesService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatusCode
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.net.URISyntaxException
import java.util.UUID

internal class BeerControllerTest {
    private val beerService = mockk<BeerService>()
    private val localesService = mockk<LocalesService>()

    private lateinit var testSubject: BeerController
    private lateinit var beer: Beer
    private lateinit var beerWithCalendarAndDay: BeerWithCalendarAndDay
    private lateinit var beerWithCalendarDayAndReview: BeerWithCalendarDayAndReview

    @BeforeEach
    fun setUp() {
        beer = getBeer()
        beerWithCalendarAndDay = getBeerWithCalendarAndDay()
        beerWithCalendarDayAndReview = getBeerWithCalendarDayAndReview()
        testSubject = BeerController(beerService, localesService)
    }

    @Test
    fun testGetBeers() {
        every { beerService.all } returns setOf(beer)
        val beerCalendars = testSubject.beers

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(1, beerCalendars.getBody()?.size) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testGetBeersWithReviewByCalendar() {
        every { beerService.getBeersWithReviewByCalendarAndUser(any(), any()) } returns setOf(beerWithCalendarDayAndReview)
        val beerCalendars = testSubject.getBeersWithReviewByCalendar(
                beerWithCalendarAndDay.calendar?.id!!
        )

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(1, beerCalendars.getBody()?.size) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testGetBeersWithReviewByCalendarAndUser() {
        every { beerService.getBeersWithReviewByCalendarAndUser(any(), any()) } returns setOf(beerWithCalendarDayAndReview)
        val beerCalendars = testSubject.getBeersWithReviewByCalendarAndUser(
                beerWithCalendarAndDay.calendar?.id!!, beerWithCalendarAndDay.brewer.id!!
        )

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(1, beerCalendars.getBody()?.size) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testGetAllBeersWithCalendar() {
        every { beerService.getBeersWithCalendar(null, null) } returns setOf(beerWithCalendarAndDay)
        val beerCalendars = testSubject.allBeersWithCalendar

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(1, beerCalendars.getBody()?.size) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testGetAllBeersWithCalendarExport() {
        val locale = "no"
        every { localesService.getString(any(), any()) } returns "beers"
        every { beerService.getBeersWithCalendarXlsx(null, null, locale) } returns ByteArray(0)

        val xlsx = testSubject.getAllBeersWithCalendarExport(locale)

        Assertions.assertAll(
                { Assertions.assertNotNull(xlsx) },
                { Assertions.assertNotNull(xlsx.statusCode) },
                { Assertions.assertNotNull(xlsx.getBody()) },
                { Assertions.assertNotNull(xlsx.headers) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), xlsx.statusCode) },
                {
                    Assertions.assertEquals(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            xlsx.headers.get("Content-Type")?.first()
                    )
                },
                {
                    Assertions.assertEquals(
                            "attachment; filename=\"beers\"",
                            xlsx.headers.get("Content-Disposition")?.first()
                    )
                }
        )
    }

    @Test
    fun testGetTodaysBeersWithCalendarAndReview() {
        every { beerService.getBeersWithCalendarAndReviewByDate(any()) } returns setOf(beerWithCalendarDayAndReview)
        val beerCalendars = testSubject.todaysBeersWithCalendarAndReview

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(1, beerCalendars.getBody()?.size) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testGetBeersWithCalendar() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns beer.brewer.email
        every { beerService.getBeersWithCalendar(any(), any()) } returns setOf(beerWithCalendarAndDay)
        val beerCalendars = testSubject.beersWithCalendar

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(1, beerCalendars.getBody()?.size) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testGetBeersWithCalendarByCalendar() {
        every { beerService.getBeersWithCalendar(any(), any()) } returns setOf(beerWithCalendarAndDay)
        val beerCalendars = testSubject.getBeersWithCalendarByCalendar(
                beerWithCalendarAndDay.calendar?.id!!
        )

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(1, beerCalendars.getBody()?.size) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testGetBeerById() {
        every { beerService.getById(any()) } returns beer
        val beerCalendars = testSubject.getBeerById(UUID.randomUUID())

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    @Throws(URISyntaxException::class)
    fun testCreateBeer() {
        every { beerService.create(any()) } returns beer
        val beer = testSubject.createBeer(this.beer)

        Assertions.assertAll(
                { Assertions.assertNotNull(beer) },
                { Assertions.assertNotNull(beer.statusCode) },
                { Assertions.assertNotNull(beer.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(201), beer.statusCode) }
        )
    }

    @Test
    fun testUpdateBeer() {
        every { beerService.update(any(), any()) } returns beer
        val beerCalendars = testSubject.updateBeer(beer.id, beer)

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testDeleteBeer() {
        every { beerService.delete(any()) } returns true
        val beerCalendars = testSubject.deleteBeer(UUID.randomUUID())

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(204), beerCalendars.statusCode) }
        )
    }
}