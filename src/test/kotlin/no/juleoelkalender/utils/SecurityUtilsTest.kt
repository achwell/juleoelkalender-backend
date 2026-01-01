package no.juleoelkalender.utils

import no.juleoelkalender.entity.AuthorityEntity
import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.getAuthorityEntityUser
import no.juleoelkalender.getUserEntity
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import org.springframework.security.core.authority.SimpleGrantedAuthority
import kotlin.test.assertEquals

internal class SecurityUtilsTest {
    private val secretKey = "TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST"
    private val authorityEntityUser: AuthorityEntity = getAuthorityEntityUser()
    private val userEntity: UserEntity = getUserEntity()

    @Test
    fun testGetSignInKey() {
        val signInKey = getSignInKey(secretKey)
        assertAll(
                { assertNotNull(signInKey) },
                { assertEquals("HmacSHA384", signInKey.algorithm) }
        )
    }

    @Test
    fun testExtractAllClaims() {
        val token = buildToken(
                mutableMapOf<String, Any>(), userEntity.email,
                listOf(SimpleGrantedAuthority("ROLE_USER")), secretKey, JWT_EXPIRES_TIMEOUT_MS
        )
        val claims = extractAllClaims(token, secretKey)
        assertAll(
                { assertNotNull(claims) },
                { assertEquals(userEntity.email, claims.subject) }
        )
    }

    @Test
    fun testExtractClaim() {
        val token = buildToken(
                extraClaims = mutableMapOf<String, Any>(),
                username = authorityEntityUser.name,
                authorities = listOf(SimpleGrantedAuthority("ROLE_USER")),
                jwtKey = secretKey,
                jwtExpiresTimeoutMs = JWT_EXPIRES_TIMEOUT_MS
        )

        val subject: String = extractClaim(token, secretKey) { it.subject }
        val authorities: String = extractClaim(token, secretKey) { it.get("authorities", String::class.java) }
        assertAll(
                { assertNotNull(subject) },
                { assertNotNull(authorities) },
                { assertEquals(userEntity.email, subject) },
                { assertEquals(authorityEntityUser.name, authorities) }
        )
    }

    @Test
    fun testIsTokenExpired() {
        val token = buildToken(
                extraClaims = mutableMapOf<String, Any>(),
                username = authorityEntityUser.name,
                authorities = listOf(SimpleGrantedAuthority("ROLE_USER")),
                jwtKey = secretKey,
                jwtExpiresTimeoutMs = JWT_EXPIRES_TIMEOUT_MS
        )
        assertFalse(isTokenExpired(token, secretKey))
    }

    @Test
    fun testBuildToken() {
        val token = buildToken(
                extraClaims = mutableMapOf<String, Any>(),
                username = authorityEntityUser.name,
                authorities = listOf(SimpleGrantedAuthority("ROLE_USER")),
                jwtKey = secretKey,
                jwtExpiresTimeoutMs = JWT_EXPIRES_TIMEOUT_MS
        )
        assertNotNull(token)
    }

    companion object {
        private const val JWT_EXPIRES_TIMEOUT_MS = 14400000L
    }
}
