package no.juleoelkalender.filter

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.getUserEntity
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.utils.buildToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.test.assertEquals

internal class JWTTokenValidatorFilterTest {
    private val userRepository = mockk<UserRepository>()
    private val jwtKey = "1234567890abcdefghijklmnopqrstuvwxyz"
    private lateinit var token: String
    private lateinit var testSubject: JWTTokenValidatorFilter

    @BeforeEach
    fun setUp() {
        token = buildToken(
            extraClaims = HashMap<String, Any>(),
            username = "a@b.c",
            authorities = listOf(SimpleGrantedAuthority("A"), SimpleGrantedAuthority("B")),
            jwtKey = jwtKey,
            jwtExpiresTimeoutMs = 3600000L
        )
        testSubject = JWTTokenValidatorFilter(jwtKey, userRepository)
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun validBearerTokenAuthenticatesExistingUser() {
        every { userRepository.findByEmailIgnoreCase("a@b.c") } returns getUserEntity()
        val request = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
        val response = MockHttpServletResponse()

        testSubject.doFilterInternal(request, response, MockFilterChain())

        assertEquals(HttpStatus.OK.value(), response.status)
        assertEquals("a@b.c", SecurityContextHolder.getContext().authentication?.name)
    }

    @Test
    fun malformedBearerTokenIsUnauthorized() {
        val request = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid")
        }
        val response = MockHttpServletResponse()

        testSubject.doFilterInternal(request, response, MockFilterChain())

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status)
    }

    @Test
    fun tokenForUnknownUserIsUnauthorized() {
        every { userRepository.findByEmailIgnoreCase("a@b.c") } returns null
        val request = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
        val response = MockHttpServletResponse()

        testSubject.doFilterInternal(request, response, MockFilterChain())

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status)
    }

    @Test
    fun basicAuthenticationHeaderIsNotParsedAsJwt() {
        val request = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
        }
        val response = MockHttpServletResponse()

        testSubject.doFilterInternal(request, response, MockFilterChain())

        assertEquals(HttpStatus.OK.value(), response.status)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun testShouldNotFilter() {
        val request = MockHttpServletRequest().apply { servletPath = "/actuator" }
        assertTrue(testSubject.shouldNotFilter(request))
    }

    @Test
    fun testShouldFilter() {
        val request = MockHttpServletRequest().apply { servletPath = "/api/v1/users" }
        assertFalse(testSubject.shouldNotFilter(request))
    }
}
