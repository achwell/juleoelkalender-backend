package no.juleoelkalender.controller

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.getBeerStyle
import no.juleoelkalender.model.BeerStyle
import no.juleoelkalender.service.BeerStyleService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatusCode
import java.net.URISyntaxException
import java.util.UUID

internal class BeerStyleControllerTest {
    private val beerStyleService = mockk<BeerStyleService>()

    private lateinit var testSubject: BeerStyleController

    private lateinit var beerStyle: BeerStyle

    @BeforeEach
    fun setUp() {
        beerStyle = getBeerStyle()
        testSubject = BeerStyleController(beerStyleService)
    }

    @Test
    fun testGetBeerStyles() {
        every { beerStyleService.all } returns setOf(beerStyle)
        val beerStyles = testSubject.beerStyles

        Assertions.assertAll(
                { Assertions.assertNotNull(beerStyles) },
                { Assertions.assertNotNull(beerStyles.statusCode) },
                { Assertions.assertNotNull(beerStyles.getBody()) },
                { Assertions.assertEquals(1, beerStyles.getBody()!!.size) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerStyles.statusCode) }
        )
    }

    @Test
    fun testGetBeerStyleById() {
        every { beerStyleService.getById(any()) } returns beerStyle
        val beerStyle = testSubject.getBeerStyleById(UUID.randomUUID())

        Assertions.assertAll(
                { Assertions.assertNotNull(beerStyle) },
                { Assertions.assertNotNull(beerStyle.statusCode) },
                { Assertions.assertNotNull(beerStyle.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerStyle.statusCode) }
        )
    }

    @Test
    @Throws(URISyntaxException::class)
    fun testCreateBeerStyle() {
        every { beerStyleService.create(any()) } returns beerStyle
        val beer = testSubject.createBeerStyle(beerStyle)

        Assertions.assertAll(
                { Assertions.assertNotNull(beer) },
                { Assertions.assertNotNull(beer.statusCode) },
                { Assertions.assertNotNull(beer.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(201), beer.statusCode) }
        )
    }

    @Test
    fun testUpdateBeerStyle() {
        every { beerStyleService.update(any(), any()) } returns beerStyle
        val beerCalendars = testSubject.updateBeerStyle(beerStyle.id!!, beerStyle)

        Assertions.assertAll(
                { Assertions.assertNotNull(beerCalendars) },
                { Assertions.assertNotNull(beerCalendars.statusCode) },
                { Assertions.assertNotNull(beerCalendars.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(200), beerCalendars.statusCode) }
        )
    }

    @Test
    fun testDeleteBeerStyle() {
        every { beerStyleService.delete(any()) } returns true
        val beerStyle = testSubject.deleteBeerStyle(beerStyle.id!!)

        Assertions.assertAll(
                { Assertions.assertNotNull(beerStyle) },
                { Assertions.assertNotNull(beerStyle.statusCode) },
                { Assertions.assertNull(beerStyle.getBody()) },
                { Assertions.assertEquals(HttpStatusCode.valueOf(204), beerStyle.statusCode) }
        )
    }
}