package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(name = "CalendarToken", description = "Et tilgangstoken til en kalendergruppe.")
data class CalendarToken(
    val id: UUID, @field:NotNull var token: String, @field:NotNull var name: String, @field:NotNull var active: Boolean
)
