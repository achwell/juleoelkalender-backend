package no.juleoelkalender.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import jakarta.mail.MessagingException
import no.juleoelkalender.config.MailProperties
import no.juleoelkalender.entity.PasswordChangeRequestEntity
import no.juleoelkalender.getPasswordChangeRequestEntity
import no.juleoelkalender.mappers.PasswordChangeRequestMapper
import no.juleoelkalender.model.PasswordChangeRequest
import no.juleoelkalender.repository.PasswordChangeRequestRepository
import no.juleoelkalender.service.impl.PasswordChangeRequestServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID

internal class PasswordChangeRequestServiceTest {
    private val passwordChangeRequestRepository = mockk<PasswordChangeRequestRepository>()
    private val emailService = mockk<EmailService>()
    private val passwordChangeRequestMapper = PasswordChangeRequestMapper()

    private lateinit var testSubject: PasswordChangeRequestService
    private lateinit var passwordChangeRequestEntity: PasswordChangeRequestEntity

    @BeforeEach
    fun setUp() {
        passwordChangeRequestEntity = getPasswordChangeRequestEntity()
        val mailProperties = MailProperties()
        mailProperties.baseUrl = "http://localhost"
        mailProperties.from = "first@last.no"
        mailProperties.supportEmail = "first@last.no"
        testSubject = PasswordChangeRequestServiceImpl(passwordChangeRequestRepository, emailService, passwordChangeRequestMapper, mailProperties, ByteArrayResource("".toByteArray()))
    }

    @Test
    fun testGetPasswordChangeRequests() {
        every { passwordChangeRequestRepository.findAll() } returns listOf(passwordChangeRequestEntity, passwordChangeRequestEntity, passwordChangeRequestEntity)
        val passwordChangeRequests = testSubject.all
        assertAll(
                { assertNotNull(passwordChangeRequests) },
                { assertEquals(1, passwordChangeRequests.size) }
        )
    }

    @Test
    fun testGetPasswordChangeRequestById() {
        every { passwordChangeRequestRepository.findById(any()) } returns Optional.of(passwordChangeRequestEntity)
        val passwordChangeRequest = testSubject.getById(
                passwordChangeRequestEntity.id
        )
        assertAll(
                { assertNotNull(passwordChangeRequest) },
                { assertEquals(passwordChangeRequestEntity.id, passwordChangeRequest?.id) }
        )
    }

    @Test
    @Throws(MessagingException::class)
    fun testCreatePasswordChangeRequest() {
        val pcr = passwordChangeRequestMapper.entityToModel(
                passwordChangeRequestEntity
        )
        val passwordChangeRequest = PasswordChangeRequest(
                UUID.randomUUID(), pcr.token,
                pcr.email, ZonedDateTime.now()
        )
        every { passwordChangeRequestRepository.save(any()) } returns passwordChangeRequestEntity
        every { emailService.sendSimpleMessage(any(), any(), any(), any()) } just Runs
        val newPasswordChangeRequest = testSubject.create(passwordChangeRequest)
        assertAll(
                { assertNotNull(newPasswordChangeRequest) },
                { assertEquals(passwordChangeRequestEntity.id, newPasswordChangeRequest.id) },
                { assertEquals(passwordChangeRequestEntity.token, newPasswordChangeRequest.token) },
                { assertEquals(passwordChangeRequestEntity.email, newPasswordChangeRequest.email) }
        )
    }

    @Test
    fun testUpdatePasswordChangeRequest() {
        val passwordChangeRequest = passwordChangeRequestMapper.entityToModel(
                passwordChangeRequestEntity
        )
        every { passwordChangeRequestRepository.findById(any()) } returns Optional.of<PasswordChangeRequestEntity>(passwordChangeRequestEntity)
        every { passwordChangeRequestRepository.save(any()) } returns passwordChangeRequestEntity
        val updatedCalendarToken = testSubject.update(
                passwordChangeRequestEntity.id, passwordChangeRequest
        )
        assertAll(
                { assertNotNull(updatedCalendarToken) },
                { assertEquals(passwordChangeRequestEntity.id, updatedCalendarToken?.id) },
                { assertEquals(passwordChangeRequest.token, updatedCalendarToken?.token) },
                { assertEquals(passwordChangeRequestEntity.email, updatedCalendarToken?.email) }
        )
    }

    @Test
    fun testDeletePasswordChangeRequestExist() {
        every { passwordChangeRequestRepository.existsById(any()) } returns true
        every { passwordChangeRequestRepository.deleteById(any()) } just Runs
        val deleted = testSubject.delete(passwordChangeRequestEntity.id)
        assertTrue(deleted)
    }

    @Test
    fun testDeletePasswordChangeRequestDontExist() {
        every { passwordChangeRequestRepository.existsById(any()) } returns false
        val deleted = testSubject.delete(passwordChangeRequestEntity.id)
        assertFalse(deleted)
    }
}
