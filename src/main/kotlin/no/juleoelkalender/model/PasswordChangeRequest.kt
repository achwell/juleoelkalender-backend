package no.juleoelkalender.model

import java.time.ZonedDateTime
import java.util.UUID

data class PasswordChangeRequest(
        val id: UUID, val token: String, val email: String, val created: ZonedDateTime
)
