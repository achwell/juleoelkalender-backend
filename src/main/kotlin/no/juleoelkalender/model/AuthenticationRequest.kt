package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(name = "AuthenticationRequest", description = "Innloggingsinformasjon med e-postadresse og passord.")
data class AuthenticationRequest(@field:NotNull var email: String, @field:NotNull var password: String)
