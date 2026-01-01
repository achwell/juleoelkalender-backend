package no.juleoelkalender.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.impl.DefaultClock
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.GrantedAuthority
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.function.Function
import javax.crypto.SecretKey

fun getSignInKey(jwtKey: String): SecretKey {
    return Keys.hmacShaKeyFor(jwtKey.toByteArray(StandardCharsets.UTF_8))
}

fun extractAllClaims(token: String, jwtKey: String): Claims {
    return Jwts.parser().verifyWith(getSignInKey(jwtKey)).build().parseSignedClaims(token).getPayload()
}

fun <T> extractClaim(token: String, jwtKey: String, claimsResolver: Function<Claims, T>): T {
    val claims = extractAllClaims(token, jwtKey)
    return claimsResolver.apply(claims)
}

fun isTokenExpired(token: String, jwtKey: String): Boolean {
    return extractExpiration(token, jwtKey).before(Date())
}

fun buildToken(extraClaims: Map<String, *>, username: String, authorities: Collection<GrantedAuthority>, jwtKey: String, jwtExpiresTimeoutMs: Long): String {
    val createdDate = DefaultClock.INSTANCE.now()
    val expirationDate = calculateExpirationDate(createdDate, jwtExpiresTimeoutMs)
    return Jwts.builder().issuer("Juleølkalender").claims(extraClaims).subject(username).claim("username", username).claim("authorities", populateAuthorities(authorities)).issuedAt(createdDate).expiration(expirationDate).signWith(getSignInKey(jwtKey)).compact()
}

private fun calculateExpirationDate(createdDate: Date, jwtExpiresTimeoutMs: Long): Date {
    return Date(createdDate.time + jwtExpiresTimeoutMs)
}

private fun populateAuthorities(collection: Collection<GrantedAuthority>): String {
    return collection.map { it.authority }.toSet().joinToString(separator = ",")
}

private fun extractExpiration(token: String, jwtKey: String): Date {
    return extractClaim<Date>(token, jwtKey) { obj: Claims -> obj.expiration }
}
