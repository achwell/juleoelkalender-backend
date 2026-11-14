package no.juleoelkalender.model.externalauth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Profilbilde fra Facebook.")
data class FacebookPicture(
    @field:Schema(description = "Informasjon om profilbildet.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var data: FacebookPictureData?
)
