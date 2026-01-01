package no.juleoelkalender.service.impl

import jakarta.mail.MessagingException
import no.juleoelkalender.service.EmailService
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
class EmailServiceImpl(private val emailSender: JavaMailSender) : EmailService {
    @Throws(MessagingException::class)
    override fun sendSimpleMessage(from: String, to: String, subject: String, text: String) {
        val message = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, StandardCharsets.UTF_8.name())
        helper.setFrom(from)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(text, true)
        emailSender.send(message)
    }
}