package no.juleoelkalender.model.externalauth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Brukeropplysninger fra Facebook.")
data class FacebookAuthenticationRequest(
    var id: String, var firstName: String,
    @field:Schema(description = "Brukerens mellomnavn.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var middleName: String?,
    var lastName: String, var email: String,
    @field:Schema(description = "Brukerens profilbilde.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var picture: FacebookPicture?
)