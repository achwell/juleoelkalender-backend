package no.juleoelkalender.mappers

import no.juleoelkalender.entity.BeerStyleEntity
import no.juleoelkalender.getBeerStyle
import no.juleoelkalender.getBeerStyleEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BeerStyleMapperTest {
    private lateinit var testSubject: BeerStyleMapper
    private lateinit var beerStyleEntity: BeerStyleEntity

    @BeforeEach
    fun setUp() {
        testSubject = BeerStyleMapper()
        beerStyleEntity = getBeerStyleEntity()
    }

    @Test
    fun testEntityToModel() {
        val beerStyle = testSubject.entityToModel(beerStyleEntity)
        assertAll(
                { assertNotNull(beerStyle) },
                { assertEquals(beerStyleEntity.id, beerStyle.id) },
                { assertEquals(beerStyleEntity.name, beerStyle.name) }
        )
    }

    @Test
    fun testModelToEntity() {
        val beerCalendarEntity = testSubject.modelToEntity(getBeerStyle())
        assertAll(
                { assertNotNull(beerCalendarEntity) },
                { assertEquals(getBeerStyle().id, beerCalendarEntity.id) },
                { assertEquals(getBeerStyle().name, beerCalendarEntity.name) }
        )
    }
}
