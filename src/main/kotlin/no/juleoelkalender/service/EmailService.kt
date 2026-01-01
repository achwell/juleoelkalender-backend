package no.juleoelkalender.service

import jakarta.mail.MessagingException

interface EmailService {
    @Throws(MessagingException::class)
    fun sendSimpleMessage(from: String, to: String, subject: String, text: String)
}