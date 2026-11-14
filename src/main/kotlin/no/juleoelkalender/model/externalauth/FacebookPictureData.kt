package no.juleoelkalender.model.externalauth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Adresse og dimensjoner for et Facebook-profilbilde.")
data class FacebookPictureData(var url: String, var height: Int, var width: Int, var isSilhouette: Boolean)
