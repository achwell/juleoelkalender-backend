package no.juleoelkalender.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.JwtValidationException
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JwtCompatibilityTest {
    private val legacyTokens = requireNotNull(javaClass.getResource("/jwt/tokens.txt"))
        .readText().lineSequence().filter { it.isNotBlank() }.map {
            val (length, token) = it.split(" ", limit = 2)
            "a".repeat(length.toInt()) to token
        }.toList()

    @Test
    fun `accept existing JJWT tokens for every automatically selected HMAC algorithm`() {
        for ((secret, token) in legacyTokens) {
            val jwt = extractAllClaims(token, secret)
            assertEquals("legacy@example.no", jwt.subject)
            assertEquals("legacy@example.no", jwt.getClaimAsString("username"))
            assertEquals("ROLE_USER,ROLE_ADMIN", jwt.getClaimAsString("authorities"))
            assertEquals("Juleølkalender", jwt.getClaimAsString("iss"))
            assertEquals(Instant.parse("2099-01-01T00:00:00Z"), jwt.expiresAt)
        }
    }

    @Test
    fun `new tokens preserve legacy algorithms keys and claims`() {
        for ((secret, legacyToken) in legacyTokens) {
            val token = buildToken(
                mapOf("custom" to "value"), "legacy@example.no",
                listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN")), secret, 60000
            )
            val parts = token.split(".")
            val mac = Mac.getInstance(getSignInKey(secret).algorithm)
            mac.init(getSignInKey(secret))
            assertTrue(
                MessageDigest.isEqual(
                    mac.doFinal("${parts[0]}.${parts[1]}".toByteArray(UTF_8)),
                    Base64.getUrlDecoder().decode(parts[2])
                )
            )
            val jwt = extractAllClaims(token, secret)
            assertEquals(extractAllClaims(legacyToken, secret).headers["alg"], jwt.headers["alg"])
            assertEquals("legacy@example.no", jwt.subject)
            assertEquals("legacy@example.no", jwt.getClaimAsString("username"))
            assertEquals("ROLE_USER,ROLE_ADMIN", jwt.getClaimAsString("authorities"))
            assertEquals("Juleølkalender", jwt.getClaimAsString("iss"))
            assertEquals("value", jwt.getClaimAsString("custom"))
            assertEquals(60, jwt.expiresAt!!.epochSecond - jwt.issuedAt!!.epochSecond)
        }
    }

    @Test
    fun `reject invalid signatures and malformed tokens`() {
        for ((secret, token) in legacyTokens) {
            assertThrows<BadJwtException> { extractAllClaims(token, "b".repeat(secret.length)) }
            val parts = token.split(".")
            val signature = Base64.getUrlDecoder().decode(parts[2])
            signature[0] = (signature[0].toInt() xor 1).toByte()
            val tampered = "${parts[0]}.${parts[1]}.${Base64.getUrlEncoder().withoutPadding().encodeToString(signature)}"
            assertThrows<BadJwtException> { extractAllClaims(tampered, secret) }
        }
        assertThrows<BadJwtException> { extractAllClaims("invalid", "a".repeat(32)) }
    }

    @Test
    fun `reject expired tokens without clock skew and future not before claims`() {
        val secret = "a".repeat(32)
        val expired = requireNotNull(javaClass.getResource("/jwt/expired-token.txt")).readText().trim()
        assertThrows<JwtValidationException> { extractAllClaims(expired, secret) }
        val future = buildToken(mapOf("nbf" to Instant.now().plusSeconds(3600)), "user", emptyList(), secret, 7200000)
        assertThrows<JwtValidationException> { extractAllClaims(future, secret) }
    }

    @Test
    fun `reject weak keys`() {
        assertThrows<IllegalArgumentException> { getSignInKey("a".repeat(31)) }
    }
}
