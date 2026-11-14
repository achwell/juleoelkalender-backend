package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime
import java.util.UUID

@Schema(name = "PasswordChangeRequest", description = "En forespørsel om å endre passord.")
data class PasswordChangeRequest(
    @field:NotNull var id: UUID, @field:NotNull var token: String, @field:NotNull var email: String, @field:NotNull var created: ZonedDateTime
)
