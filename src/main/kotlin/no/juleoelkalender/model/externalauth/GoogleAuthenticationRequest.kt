package no.juleoelkalender.model.externalauth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Autentiserings- og brukeropplysninger fra Google.")
data class GoogleAuthenticationRequest(
    var credential: String,
    var email: String,
    var familyName: String,
    var givenName: String,
    @field:Schema(description = "Adresse til brukerens profilbilde.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var picture: String?
)
