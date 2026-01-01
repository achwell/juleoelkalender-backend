package no.juleoelkalender.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Email
import org.jspecify.annotations.NullMarked
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.ZonedDateTime
import java.util.UUID

data class User(var id: UUID?, var firstName: String, var middleName: String?, var lastName: String, var email: @Email String, @JsonIgnore var pwd: String,
                var area: String?, var role: Role, var locked: Boolean, var beers: Set<Beer> = setOf(), var calendarToken: Set<CalendarToken> = setOf(),
                var lastLoginDate: ZonedDateTime?, var createdDate: ZonedDateTime, var facebookUserId: String?, var imageUrl: String?,
                var imageHeight: Int?, var imageWidth: Int?, var imageSilhouette: Boolean) : UserDetails {

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
