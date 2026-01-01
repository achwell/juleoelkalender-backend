package no.juleoelkalender.monitoring

import org.springframework.boot.mail.health.MailHealthIndicator
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Component

@Component
class EmailHealthIndicator(mailSender: JavaMailSenderImpl) : MailHealthIndicator(mailSender)