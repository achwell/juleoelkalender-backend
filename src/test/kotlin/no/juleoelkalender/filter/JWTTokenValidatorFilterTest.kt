package no.juleoelkalender.filter

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.getUserEntity
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.utils.buildToken
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import kotlin.test.assertEquals

internal class JWTTokenValidatorFilterTest {
    private lateinit var testSubject: JWTTokenValidatorFilter

    private val userRepository = mockk<UserRepository>()
    private val jwtKey = "1234567890abcdefghijklmnopqrstuvwxyz"
    private lateinit var token: String
    private lateinit var userEntity: UserEntity

    @BeforeEach
    fun setUp() {
        userEntity = getUserEntity()
        token = buildToken(
                extraClaims = HashMap<String, Any>(),
                username = "a@b.c",
                authorities = listOf(SimpleGrantedAuthority("A"), SimpleGrantedAuthority("B")),
                jwtKey = jwtKey,
                jwtExpiresTimeoutMs = Long.MAX_VALUE
        )
        testSubject = JWTTokenValidatorFilter(jwtKey, userRepository)
    }


    @Test
    @Disabled("Fails with maven")
    fun doFilterInternal() {
        try {
            val response = MockHttpServletResponse()
            val request = MockHttpServletRequest()

            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
            every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity

            testSubject.doFilterInternal(request, response, MockFilterChain())
            assertEquals(HttpStatus.OK.value(), response.status)
        } catch (e: Exception) {
            fail("Should not throw Exception, but got " + e.javaClass.getName())
        }
    }

    @Test
    fun doFilterInternalBadToken() {
        try {
            val response = MockHttpServletResponse()
            val request = MockHttpServletRequest()
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer XYZ")

            testSubject.doFilterInternal(request, response, MockFilterChain())

            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status)
        } catch (e: Exception) {
            fail("Should not throw Exception, but got " + e.javaClass.getName())
        }
    }

    @Test
    fun doFilterInternalUserNotFound() {
        try {
            val response = MockHttpServletResponse()
            val request = MockHttpServletRequest()

            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
            every { userRepository.findByEmailIgnoreCase(any()) } returns null

            testSubject.doFilterInternal(request, response, MockFilterChain())

            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status)
        } catch (e: Exception) {
            fail("Should not throw Exception, but got " + e.javaClass.getName())
        }
    }

    @Test
    fun testShouldNotFilter() {
        val request = MockHttpServletRequest()
        request.servletPath = "/actuator"
        assertTrue(testSubject.shouldNotFilter(request))
    }

    @Test
    fun testShouldFilter() {
        val request = MockHttpServletRequest()
        request.servletPath = "/api/v1/users"
        assertFalse(testSubject.shouldNotFilter(request))
    }
}
