package no.juleoelkalender.model

import jakarta.validation.constraints.Email
import java.beans.Transient
import java.time.ZonedDateTime
import java.util.UUID

data class UserWithoutChildren(
        val id: UUID?, val firstName: String, val middleName: String?,
        val lastName: String,
        val email: @Email String, val password: String,
        val area: String?, val role: Role,
        val locked: Boolean, val calendarToken: Set<CalendarToken>,
        val lastLoginDate: ZonedDateTime?, val createdDate: ZonedDateTime,
        val facebookUserId: String?, val imageUrl: String?, val imageHeight: Int?,
        val imageWidth: Int?, val imageSilhouette: Boolean
) {
    @Transient
    fun name(): String {
        return listOfNotNull(firstName, middleName, lastName).joinToString { " " }
    }
}
