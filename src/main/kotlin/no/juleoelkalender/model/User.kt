package no.juleoelkalender.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import org.jspecify.annotations.NullMarked
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.ZonedDateTime
import java.util.UUID

@Schema(description = "En bruker med rolle, øl og kalendertilgang.")
data class User(
    @field:Schema(description = "Identifikator for brukeren.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var id: UUID?,
    @field:NotNull var firstName: String,
    @field:Schema(description = "Brukerens mellomnavn.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var middleName: String?,
    @field:NotNull var lastName: String, @field:NotNull @field:Email var email: String, @JsonIgnore var pwd: String,
    @field:Schema(description = "Brukerens område.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var area: String?,
    @field:NotNull var role: Role,
    @field:NotNull var locked: Boolean,
    @field:NotNull var beers: Set<Beer> = setOf(),
    @field:NotNull var calendarToken: Set<CalendarToken> = setOf(),
    @field:Schema(description = "Tidspunkt for siste innlogging.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var lastLoginDate: ZonedDateTime?,
    @field:NotNull var createdDate: ZonedDateTime,
    @field:Schema(description = "Brukerens Facebook-identifikator.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var facebookUserId: String?,
    @field:Schema(description = "Adresse til brukerens profilbilde.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var imageUrl: String?,
    @field:Schema(description = "Profilbildets høyde i piksler.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var imageHeight: Int?,
    @field:Schema(description = "Profilbildets bredde i piksler.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var imageWidth: Int?,
    @field:NotNull var imageSilhouette: Boolean
) : UserDetails {

    @get:JsonIgnore
    val name: String
        get() = listOfNotNull(this.firstName, this.middleName, this.lastName).joinToString { " " }

    @JsonIgnore
    @NullMarked
    override fun getAuthorities(): Set<GrantedAuthority> {
        val auth: MutableSet<GrantedAuthority> = mutableSetOf(role)
        role.authorities.forEach { auth.add(SimpleGrantedAuthority(it.name)) }
        return auth
    }

    override fun getPassword(): String {
        return pwd
    }

    @JsonIgnore
    override fun getUsername(): String {
        return email
    }

    @get:JsonIgnore
    val userWithoutChildren: UserWithoutChildren
        get() = UserWithoutChildren(
            id = id, firstName = firstName, middleName = middleName, lastName = lastName, email = email, password = pwd, area = area, role = role,
            locked = locked, calendarToken = calendarToken, lastLoginDate = lastLoginDate, createdDate = createdDate, facebookUserId = facebookUserId, imageUrl = imageUrl, imageHeight = imageHeight,
            imageWidth = imageWidth, imageSilhouette = imageSilhouette
        )
}
