package no.juleoelkalender.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import no.juleoelkalender.service.impl.EmailServiceImpl
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mail.javamail.JavaMailSender
import java.util.Properties

internal class EmailServiceTest {
    private val emailSender = mockk<JavaMailSender>()

    private lateinit var testSubject: EmailService

    @BeforeEach
    fun setUp() {
        testSubject = EmailServiceImpl(emailSender)
    }

    @Test
    fun sendSimpleMessage() {
        every { emailSender.createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { emailSender.send(any<MimeMessage>(), any()) } just Runs

        try {
            testSubject.sendSimpleMessage("first@last.no", "first@last.no", "subject", "content")
        } catch (e: Exception) {
            fail("Should not throw Exception, but got " + e.javaClass.getName())
        }
    }
}
