package no.juleoelkalender.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfiguration(
        @param:Value($$"${spring.mail.host}") private val host: String,
        @param:Value($$"${spring.mail.port}") private val port: Int,
        @param:Value($$"${spring.mail.username}") private val username: String,
        @param:Value($$"${spring.mail.password}") private val password: String,
        @param:Value($$"${spring.mail.properties.mail.smtp.auth}") private val auth: Boolean,
        @param:Value($$"${spring.mail.properties.mail.smtp.starttls.enable}") private val starttls: Boolean
) {
    @get:Bean
    val javaMailSender: JavaMailSender
        get() {
            val mailSender = JavaMailSenderImpl()
            mailSender.host = host
            mailSender.port = port
            mailSender.username = username
            mailSender.password = password
            val props = mailSender.javaMailProperties
            props["mail.transport.protocol"] = "smtp"
            props["mail.smtp.auth"] = auth
            props["mail.smtp.starttls.enable"] = starttls
            props["mail.debug"] = "true"
            return mailSender
        }
}
