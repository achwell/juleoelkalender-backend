package no.juleoelkalender.filter

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.ServletException
import no.juleoelkalender.config.JwtService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.io.IOException
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class JWTTokenGeneratorFilterTest {
    private lateinit var testSubject: JWTTokenGeneratorFilter

    private val jwtService = mockk<JwtService>()

    @BeforeEach
    fun setUp() {
        testSubject = JWTTokenGeneratorFilter(jwtService)
    }

    @Test
    @Throws(ServletException::class, IOException::class)
    fun doFilterInternal() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication } returns authentication
        every { authentication.name } returns ""
        every { authentication.authorities } returns listOf()
        every { jwtService.generateToken(any(), any()) } returns "token"

        val response = MockHttpServletResponse()
        testSubject.doFilterInternal(MockHttpServletRequest(), response, MockFilterChain())

        assertEquals(200, response.status)
        assertEquals("token", response.getHeader(HttpHeaders.AUTHORIZATION))
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
        request.servletPath = "/api"
        assertFalse(testSubject.shouldNotFilter(request))
    }
}
