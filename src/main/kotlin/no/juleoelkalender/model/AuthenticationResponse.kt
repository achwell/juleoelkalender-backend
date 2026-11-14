package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "AuthenticationResponse", description = "Resultatet av en autentiseringsforespørsel.")
data class AuthenticationResponse(
    @field:Schema(description = "Autentiseringstoken.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val token: String?,
    @field:Schema(description = "Den autentiserte brukeren.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val user: User?,
    @field:Schema(description = "Melding om autentiseringen.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val message: String?
)
