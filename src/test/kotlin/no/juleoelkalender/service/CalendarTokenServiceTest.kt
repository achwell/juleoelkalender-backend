package no.juleoelkalender.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.exception.ConflictException
import no.juleoelkalender.getCalendarTokenEntity
import no.juleoelkalender.mappers.CalendarTokenMapper
import no.juleoelkalender.model.CalendarToken
import no.juleoelkalender.repository.CalendarTokenRepository
import no.juleoelkalender.service.impl.CalendarTokenServiceImpl
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

internal class CalendarTokenServiceTest {
    private val calendarTokenRepository = mockk<CalendarTokenRepository>()
    private val calendarTokenMapper = CalendarTokenMapper()

    private lateinit var calendarTokenEntity: CalendarTokenEntity

    private lateinit var testSubject: CalendarTokenService

    @BeforeEach
    fun setUp() {
        calendarTokenEntity = getCalendarTokenEntity()
        testSubject = CalendarTokenServiceImpl(calendarTokenRepository, calendarTokenMapper)
    }

    @Test
    fun testGetCalendarTokens() {
        every { calendarTokenRepository.findAll() } returns listOf(calendarTokenEntity, calendarTokenEntity, calendarTokenEntity)
        val calendarTokens = testSubject.all
        assertAll(
            { assertNotNull(calendarTokens) },
            { assertEquals(1, calendarTokens.size) }
        )
    }

    @Test
    fun testGetCalendarTokenById() {
        every { calendarTokenRepository.findById(any()) } returns Optional.of(calendarTokenEntity)
        val calendarToken = testSubject.getById(calendarTokenEntity.id)
        assertAll(
            { assertNotNull(calendarToken) },
            { assertEquals(calendarTokenEntity.id, calendarToken?.id) }
        )
    }

    @Test
    fun testCreateCalendarToken() {
        every { calendarTokenRepository.save(any()) } returns calendarTokenEntity
        val calendarToken = testSubject.create(
            calendarTokenMapper.entityToModel(
                calendarTokenEntity
            )
        )
        assertAll(
            { assertNotNull(calendarToken) },
            { assertEquals(calendarTokenEntity.id, calendarToken.id) },
            { assertEquals(calendarTokenEntity.token, calendarToken.token) },
            { assertEquals(calendarTokenEntity.name, calendarToken.name) },
            { assertTrue(calendarToken.active) }
        )
    }

    @Test
    fun testUpdateCalendarToken() {
        val c = calendarTokenMapper.entityToModel(calendarTokenEntity)
        val calendarToken = CalendarToken(c.id, "UPDATED TOKEN", "UPDATED NAME", c.active)
        every { calendarTokenRepository.findById(any()) } returns Optional.of(calendarTokenEntity)
        every { calendarTokenRepository.save(any()) } returns calendarTokenMapper.modelToEntity(calendarToken)

        val updatedCalendarToken = testSubject.update(calendarTokenEntity.id, calendarToken)
        assertAll(
            { assertNotNull(updatedCalendarToken) },
            { assertEquals(calendarTokenEntity.id, updatedCalendarToken?.id) },
            { assertEquals(calendarToken.token, updatedCalendarToken?.token) },
            { assertEquals(calendarTokenEntity.name, updatedCalendarToken?.name) },
            { assertTrue(updatedCalendarToken!!.active) }
        )
    }

    @Test
    fun testUpdateCalendarTokenLastActive() {
        val c = calendarTokenMapper.entityToModel(calendarTokenEntity)
        val calendarToken = CalendarToken(c.id, "UPDATED TOKEN", "UPDATED NAME", false)
        every { calendarTokenRepository.findById(any()) } returns Optional.of(calendarTokenEntity)
        every { calendarTokenRepository.countByActiveTrueAndIdNot(calendarTokenEntity.id) } returns 0L

        val thrown = assertThrows<ConflictException> { testSubject.update(calendarTokenEntity.id, calendarToken) }
        assertNotNull(thrown)
    }

    @Test
    fun testDeleteCalendarTokenExist() {
        every { calendarTokenRepository.countByActiveTrueAndIdNot(calendarTokenEntity.id) } returns 1L
        every { calendarTokenRepository.existsById(any()) } returns true
        every { calendarTokenRepository.deleteById(any()) } just Runs

        val deleted = testSubject.delete(calendarTokenEntity.id)
        assertTrue(deleted)
    }

    @Test
    fun testDeleteCalendarTokenDontExist() {
        every { calendarTokenRepository.countByActiveTrueAndIdNot(calendarTokenEntity.id) } returns 1L
        every { calendarTokenRepository.existsById(any()) } returns false
        val deleted = testSubject.delete(calendarTokenEntity.id)
        assertFalse(deleted)
    }

    @Test
    fun testDeleteCalendarTokenLastActive() {
        every { calendarTokenRepository.countByActiveTrueAndIdNot(calendarTokenEntity.id) } returns 0L
        val thrown = assertThrows<ConflictException> { testSubject.delete(calendarTokenEntity.id) }
        assertNotNull(thrown)
    }
}
