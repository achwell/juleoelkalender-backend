package no.juleoelkalender.controller

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import jakarta.mail.MessagingException
import jakarta.servlet.http.HttpServletRequest
import no.juleoelkalender.exception.InvalidTokenException
import no.juleoelkalender.getRegisterRequest
import no.juleoelkalender.getUser
import no.juleoelkalender.model.*
import no.juleoelkalender.model.externalauth.FacebookAuthenticationRequest
import no.juleoelkalender.model.externalauth.GoogleAuthenticationRequest
import no.juleoelkalender.service.AuthenticationService
import no.juleoelkalender.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatusCode
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.net.URISyntaxException

internal class AuthControllerTest {

    private val authenticationService = mockk<AuthenticationService>()
    private val userService = mockk<UserService>()

    private lateinit var testSubject: AuthController
    private lateinit var request: RegisterRequest
    private lateinit var user: User


    @BeforeEach
    fun setUp() {
        request = getRegisterRequest()
        user = getUser()
        testSubject = AuthController(authenticationService, userService)
    }

    @Test
    @Throws(MessagingException::class, URISyntaxException::class)
    fun testRegister() {
        every { authenticationService.register(any()) } returns AuthenticationResponse(null, null, null)
        val register = testSubject.register(request)

        assertAll(
                { assertNotNull(register) },
                { assertNotNull(register.statusCode) },
                { assertNotNull(register.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(201), register.statusCode) }
        )
    }

    @Test
    fun testAuthenticate() {
        val request = AuthenticationRequest("", "")
        every { authenticationService.authenticate(any()) } returns AuthenticationResponse(null, null, null)
        val register = testSubject.authenticate(request)

        assertAll(
                { assertNotNull(register) },
                { assertNotNull(register.statusCode) },
                { assertNotNull(register.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(200), register.statusCode) }
        )
    }

    @Test
    fun testLoginWithFacebook() {
        val request = FacebookAuthenticationRequest("", "", "", "", "", null)
        every { authenticationService.facebookAuthenticate(any()) } returns AuthenticationResponse(null, null, null)
        val register = testSubject.loginWithFacebook(request)

        assertAll(
                { assertNotNull(register) },
                { assertNotNull(register.statusCode) },
                { assertNotNull(register.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(200), register.statusCode) }
        )
    }

    @Test
    fun testLoginWithGoogle() {
        val request = GoogleAuthenticationRequest("", "", "", "", null)
        every { authenticationService.googleAuthenticate(any()) } returns AuthenticationResponse(null, null, null)
        val register = testSubject.loginWithGoogle(request)

        assertAll(
                { assertNotNull(register) },
                { assertNotNull(register.statusCode) },
                { assertNotNull(register.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(200), register.statusCode) }
        )
    }

    @Test
    fun testAddtoken() {
        val request = AddTokenRequest("", "")
        every { authenticationService.addtoken(any()) } just Runs
        val register = testSubject.addtoken(request)

        assertAll(
                { assertNotNull(register) },
                { assertNotNull(register.statusCode) },
                { assertEquals(HttpStatusCode.valueOf(200), register.statusCode) }
        )
    }

    @Test
    fun testRefresh() {
        val request: HttpServletRequest = MockHttpServletRequest()
        every { authenticationService.refresh(any()) } returns AuthenticationResponse(null, null, null)
        val register = testSubject.refresh(request)

        assertAll(
                { assertNotNull(register) },
                { assertNotNull(register.statusCode) },
                { assertNotNull(register.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(200), register.statusCode) }
        )
    }

    @Test
    fun testRefreshAuthenticationException() {
        val request: HttpServletRequest = MockHttpServletRequest()
        every { authenticationService.refresh(any()) } throws InvalidTokenException("")
        val register = testSubject.refresh(request)

        assertAll(
                { assertNotNull(register) },
                { assertNotNull(register.statusCode) },
                { assertNotNull(register.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(401), register.statusCode) }
        )
    }

    @Test
    fun testUserExist() {
        every { userService.loadUserByUsername(any()) } returns user
        val register = testSubject.userExist(user.email)

        assertAll(
                { assertNotNull(register) },
                { assertNotNull(register.statusCode) },
                { assertNotNull(register.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(200), register.statusCode) }
        )
    }

    @Test
    fun testUserExistUsernameNotFoundException() {
        every { userService.loadUserByUsername(any()) } throws UsernameNotFoundException("")
        val register = testSubject.userExist("first@last.no")

        assertAll(
                { assertNotNull(register) },
                { assertNotNull(register.statusCode) },
                { assertNotNull(register.getBody()) },
                { assertEquals(HttpStatusCode.valueOf(200), register.statusCode) }
        )
    }
}
