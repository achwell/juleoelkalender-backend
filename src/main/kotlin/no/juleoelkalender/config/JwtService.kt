package no.juleoelkalender.config

import io.jsonwebtoken.Claims
import no.juleoelkalender.model.User
import no.juleoelkalender.utils.buildToken
import no.juleoelkalender.utils.extractClaim
import no.juleoelkalender.utils.isTokenExpired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class JwtService(@param:Value($$"${app.jwt.secret_key}") private val jwtKey: String, @param:Value($$"${app.jwt.expiration_ms}") private val jwtExpiresTimeoutMs: Long) {

    fun generateToken(userDetails: UserDetails): String {
        return generateToken(userDetails.username, userDetails.authorities)
    }

    fun generateToken(name: String, authorities: Collection<GrantedAuthority>): String {
        return buildToken(
                extraClaims = emptyMap<String, Any>(),
                username = name,
                authorities = authorities,
                jwtKey = jwtKey,
                jwtExpiresTimeoutMs = jwtExpiresTimeoutMs
        )
    }

    fun extractUsername(token: String): String? {
        return extractClaim(token, jwtKey) { obj: Claims? -> obj!!.subject }
    }

    fun isTokenValid(token: String, userDetails: User): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username) && !isTokenExpired(token, jwtKey)
    }
}
