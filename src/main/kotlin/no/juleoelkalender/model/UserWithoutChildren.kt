package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import java.beans.Transient
import java.time.ZonedDateTime
import java.util.UUID

@Schema(description = "En bruker uten tilknyttede øl.")
data class UserWithoutChildren(
    @field:Schema(description = "Identifikator for brukeren.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val id: UUID?, val firstName: String,
    @field:Schema(description = "Brukerens mellomnavn.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val middleName: String?,
    val lastName: String,
    val email: @Email String, val password: String,
    @field:Schema(description = "Brukerens område.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val area: String?, val role: Role,
    val locked: Boolean, val calendarToken: Set<CalendarToken>,
    @field:Schema(description = "Tidspunkt for siste innlogging.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val lastLoginDate: ZonedDateTime?, val createdDate: ZonedDateTime,
    @field:Schema(description = "Brukerens Facebook-identifikator.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val facebookUserId: String?,
    @field:Schema(description = "Adresse til brukerens profilbilde.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val imageUrl: String?,
    @field:Schema(description = "Profilbildets høyde i piksler.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val imageHeight: Int?,
    @field:Schema(description = "Profilbildets bredde i piksler.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val imageWidth: Int?, val imageSilhouette: Boolean
) {
    @Transient
    fun name(): String {
        return listOfNotNull(firstName, middleName, lastName)
            .filter(String::isNotBlank)
            .joinToString(" ")
    }
}
