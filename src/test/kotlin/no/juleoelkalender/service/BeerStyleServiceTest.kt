package no.juleoelkalender.service

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.entity.BeerStyleEntity
import no.juleoelkalender.getBeerStyle
import no.juleoelkalender.getBeerStyleEntity
import no.juleoelkalender.mappers.BeerStyleMapper
import no.juleoelkalender.model.BeerStyle
import no.juleoelkalender.repository.BeerStyleRepository
import no.juleoelkalender.service.impl.BeerStyleServiceImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

internal class BeerStyleServiceTest {
    private lateinit var testSubject: BeerStyleService

    private val beerStyleRepository = mockk<BeerStyleRepository>()

    private lateinit var beerStyle: BeerStyle
    private lateinit var beerStyleEntity: BeerStyleEntity

    @BeforeEach
    fun setUp() {
        beerStyle = getBeerStyle()
        beerStyleEntity = getBeerStyleEntity()
        testSubject = BeerStyleServiceImpl(beerStyleRepository, BeerStyleMapper())
    }

    @Test
    fun testGetBeerStyles() {
        every { beerStyleRepository.findAll() } returns listOf(beerStyleEntity)
        val beerStyles = testSubject.all
        Assertions.assertAll(
                { Assertions.assertNotNull(beerStyles) },
                { Assertions.assertEquals(1, beerStyles.size) }
        )
    }

    @Test
    fun testGetBeerStyleById() {
        every { beerStyleRepository.findById(any()) } returns Optional.of(beerStyleEntity)
        val beerStyle = testSubject.getById(beerStyleEntity.id!!)
        Assertions.assertAll(
                { Assertions.assertNotNull(beerStyle) },
                { Assertions.assertEquals(beerStyleEntity.id, beerStyle!!.id) }
        )
    }

    @Test
    fun testCreateBeerStyle() {
        every { beerStyleRepository.save(any()) } returns beerStyleEntity
        val beerStyle = testSubject.create(this.beerStyle)
        Assertions.assertAll(
                { Assertions.assertNotNull(beerStyle) },
                { Assertions.assertEquals(beerStyleEntity.id, beerStyle.id) }
        )
    }

    @Test
    fun testUpdateBeerStyle() {
        every { beerStyleRepository.findById(any()) } returns Optional.of(beerStyleEntity)
        every { beerStyleRepository.save(any()) } returns beerStyleEntity
        val result = testSubject.update(beerStyle.id!!, beerStyle)
        Assertions.assertAll(
                { Assertions.assertNotNull(result) },
                { Assertions.assertEquals(beerStyle.id, result!!.id) }
        )
    }

    @Test
    fun testDeleteBeerStyle() {
        every { beerStyleRepository.existsById(any()) } returns true
        val deleted = testSubject.delete(beerStyleEntity.id!!)
        Assertions.assertTrue(deleted)
    }

    @Test
    fun testDeleteBeerStyleNotExist() {
        every { beerStyleRepository.existsById(any()) } returns false
        val deleted = testSubject.delete(beerStyleEntity.id!!)
        Assertions.assertFalse(deleted)
    }
}