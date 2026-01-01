package no.juleoelkalender.model

import java.util.UUID

data class CalendarToken(
        val id: UUID, val token: String, val name: String, val active: Boolean
)
