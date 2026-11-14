package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Informasjon som brukes til å registrere en bruker.")
data class RegisterRequest(
    val calendarToken: String, val firstName: String,
    @field:Schema(description = "Brukerens mellomnavn.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val middleName: String?, val lastName: String,
    @field:Schema(description = "Brukerens e-postadresse.", example = "bruker@example.com")
    val email: String, val password: String,
    @field:Schema(description = "Brukerens område.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val area: String?
)
