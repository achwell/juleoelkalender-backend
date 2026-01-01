package no.juleoelkalender.mappers

import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.getBeer
import no.juleoelkalender.getBeerEntity
import no.juleoelkalender.model.Beer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BeerMapperTest {
    private lateinit var testSubject: BeerMapper
    private lateinit var beer: Beer
    private lateinit var beerEntity: BeerEntity

    @BeforeEach
    fun setUp() {
        beer = getBeer()
        beerEntity = getBeerEntity()
        val userWithoutChildrenMapper = UserWithoutChildrenMapper(CalendarTokenMapper(), RoleMapper(AuthorityMapper()))
        testSubject = BeerMapper(userWithoutChildrenMapper)
    }

    @Test
    fun testEntityToModel() {
        val beer = testSubject.entityToModel(beerEntity)
        assertAll(
                { assertNotNull(beer) },
                { assertEquals(beerEntity.user.id, beer.brewer.id) },
                { assertEquals(beerEntity.user.email, beer.brewer.email) }
        )
    }

    @Test
    fun testModelToEntity() {
        val beerEntity = testSubject.modelToEntity(beer)
        assertAll(
                { assertNotNull(beerEntity) },
                { assertEquals(beer.brewer.id, beerEntity.user.id) },
                { assertEquals(beer.brewer.email, beerEntity.user.email) }
        )
    }
}
