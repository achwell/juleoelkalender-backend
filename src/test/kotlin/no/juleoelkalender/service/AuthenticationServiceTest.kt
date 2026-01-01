package no.juleoelkalender.service

import io.jsonwebtoken.MalformedJwtException
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import jakarta.mail.MessagingException
import jakarta.servlet.http.HttpServletRequest
import no.juleoelkalender.*
import no.juleoelkalender.config.JwtService
import no.juleoelkalender.config.MailProperties
import no.juleoelkalender.entity.*
import no.juleoelkalender.exception.InvalidTokenException
import no.juleoelkalender.mappers.*
import no.juleoelkalender.model.AddTokenRequest
import no.juleoelkalender.model.AuthenticationRequest
import no.juleoelkalender.model.AuthenticationResponse
import no.juleoelkalender.model.RegisterRequest
import no.juleoelkalender.model.externalauth.FacebookAuthenticationRequest
import no.juleoelkalender.model.externalauth.FacebookPicture
import no.juleoelkalender.model.externalauth.FacebookPictureData
import no.juleoelkalender.model.externalauth.GoogleAuthenticationRequest
import no.juleoelkalender.repository.CalendarTokenRepository
import no.juleoelkalender.repository.RoleRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.impl.AuthenticationServiceImpl
import no.juleoelkalender.utils.buildToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertTrue
import kotlin.test.fail

internal class AuthenticationServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val roleRepository = mockk<RoleRepository>()
    private val calendarTokenRepository = mockk<CalendarTokenRepository>()
    private val emailService = mockk<EmailService>()
    private val userService = mockk<UserService>()
    private val authenticationManager = mockk<AuthenticationManager>()
    private val request = mockk<HttpServletRequest>()

    private val calendarTokenMapper = CalendarTokenMapper()
    private val roleMapper = RoleMapper(AuthorityMapper())
    private val userMapper = UserMapper(BeerMapper(UserWithoutChildrenMapper(calendarTokenMapper, roleMapper)), calendarTokenMapper, roleMapper)

    private val jwtExpiresTimeoutMs: Long = 14400000
    private val jwtKey = "IKKEBRUKIKKEBRUKIKKEBRUKIKKEBRUKIKKEBRUKIKKEBRUKIKKEBRUKIKKEBRUKIKKEBRUKIKKEBRUKIKKEBRUK"

    private val now = ZonedDateTime.now()
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
    private val jwtService = JwtService(jwtKey, jwtExpiresTimeoutMs)
    private lateinit var authorityEntityUser: AuthorityEntity
    private lateinit var calendarTokenEntity: CalendarTokenEntity
    private lateinit var registerRequest: RegisterRequest
    private lateinit var roleEntityUser: RoleEntity
    private lateinit var roleEntityMaster: RoleEntity
    private lateinit var userEntity: UserEntity
    private lateinit var userEntityNoValidToken: UserEntity
    private lateinit var userEntityAdmin: UserEntity
    private lateinit var userEntityMaster: UserEntity

    private lateinit var testSubject: AuthenticationService

    @BeforeEach
    fun setUp() {
        authorityEntityUser = getAuthorityEntityUser()
        calendarTokenEntity = getCalendarTokenEntity()
        registerRequest = getRegisterRequest()
        roleEntityUser = getRoleEntityUser()
        roleEntityMaster = getRoleEntityMaster()
        userEntity = getUserEntity()
        userEntityNoValidToken = getUserEntityNoValidToken()
        userEntityAdmin = getUserEntityAdmin()
        userEntityMaster = getUserEntityMaster()
        val mailProperties = MailProperties().apply {
            baseUrl = "http://localhost"
            from = "first@last.no"
            supportEmail = "first@last.no"
        }
        testSubject = AuthenticationServiceImpl(
                userRepository, calendarTokenRepository,
                roleRepository, passwordEncoder, jwtService, authenticationManager, userMapper, roleMapper,
                emailService, userService, mailProperties, ByteArrayResource("".toByteArray())
        )
    }

    @Test
    @Throws(MessagingException::class)
    fun testRegisterFirstUser() {
        every { userRepository.findAll() } returns listOf()
        every { calendarTokenRepository.findCalendarTokenByToken(any()) } returns calendarTokenEntity
        every { roleRepository.findRoleEntityByName(any()) } returns roleEntityUser
        every { userRepository.save(any()) } returns userEntity
        every { emailService.sendSimpleMessage(any(), any(), any(), any()) } just Runs

        val authenticationResponse = testSubject.register(registerRequest)

        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNotNull(authenticationResponse.token) },
                { Assertions.assertNotNull(authenticationResponse.user) },
                { Assertions.assertNotNull(authenticationResponse.user?.role) },
                { Assertions.assertNull(authenticationResponse.message) },
                { Assertions.assertEquals(userEntity.firstName, authenticationResponse.user?.firstName) },
                { Assertions.assertEquals(userEntity.middleName, authenticationResponse.user?.middleName) },
                { Assertions.assertEquals(userEntity.lastName, authenticationResponse.user?.lastName) },
                { Assertions.assertEquals(userEntity.email, authenticationResponse.user?.email) },
                {
                    Assertions.assertEquals(
                            userEntity.role.name.name, authenticationResponse.user?.role?.name?.name
                    )
                },
                { Assertions.assertEquals(userEntity.area, authenticationResponse.user?.area) }
        )
    }

    @Test
    @Throws(MessagingException::class)
    fun testRegisterSecondUser() {
        val calendarToken2 = CalendarTokenEntity(UUID.randomUUID(), "TOKEN2", "TOKENNAME", true, mutableSetOf(), mutableSetOf(), now, now)
        val user2 = UserEntity(
                id = userEntity.id,
                firstName = userEntity.firstName, middleName = userEntity.middleName,
                lastName = userEntity.lastName, email = userEntity.email,
                password = userEntity.password, area = userEntity.area,
                role = userEntity.role, locked = userEntity.locked,
                beers = userEntity.beers, devices = mutableSetOf(), calendarToken = mutableSetOf(calendarToken2),
                reviews = userEntity.reviews, lastLoginDate = userEntity.lastLoginDate,
                createdDate = userEntity.createdDate, updatedDate = userEntity.updatedDate,
                facebookUserId = userEntity.facebookUserId, imageUrl = userEntity.imageUrl,
                imageHeight = userEntity.imageHeight, imageWidth = userEntity.imageWidth,
                imageSilhouette = userEntity.imageSilhouette
        )
        every { userRepository.findAll() } returns listOf(user2)
        every { calendarTokenRepository.findCalendarTokenByToken(any()) } returns calendarTokenEntity
        every { userRepository.save(any()) } returns userEntity
        every { emailService.sendSimpleMessage(any(), any(), any(), any()) } just Runs

        val authenticationResponse = testSubject.register(registerRequest)

        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNotNull(authenticationResponse.token) },
                { Assertions.assertNotNull(authenticationResponse.user) },
                { Assertions.assertNotNull(authenticationResponse.user?.role) },
                { Assertions.assertNull(authenticationResponse.message) },
                { Assertions.assertEquals(userEntity.firstName, authenticationResponse.user?.firstName) },
                { Assertions.assertEquals(userEntity.middleName, authenticationResponse.user?.middleName) },
                { Assertions.assertEquals(userEntity.lastName, authenticationResponse.user?.lastName) },
                { Assertions.assertEquals(userEntity.email, authenticationResponse.user?.email) },
                {
                    Assertions.assertEquals(
                            userEntity.role.name.name,
                            authenticationResponse.user?.role?.name?.name
                    )
                },
                { Assertions.assertEquals(userEntity.area, authenticationResponse.user?.area) }
        )
    }

    @Test
    fun testRegisterExistingUser() {
        every { userRepository.findAll() } returns listOf(userEntity)
        every { calendarTokenRepository.findCalendarTokenByToken(any()) } returns calendarTokenEntity
        val authenticationResponse = AtomicReference<AuthenticationResponse>()
        val thrown = assertThrows<RuntimeException> { authenticationResponse.set(testSubject.register(registerRequest)) }
        Assertions.assertAll(
                { Assertions.assertNull(authenticationResponse.get()) },
                { Assertions.assertNotNull(thrown) },
                { Assertions.assertEquals("Brukeren finnes allerede", thrown.message) }
        )
    }

    @Test
    fun testRegisterNoToken() {
        every { userRepository.findAll() } returns listOf(userEntity)
        every { calendarTokenRepository.findCalendarTokenByToken(any()) } returns null
        val authenticationResponse = AtomicReference<AuthenticationResponse?>()
        val thrown = assertThrows<RuntimeException> { authenticationResponse.set(testSubject.register(registerRequest)) }
        Assertions.assertAll(
                { Assertions.assertNull(authenticationResponse.get()) },
                { Assertions.assertNotNull(thrown) },
                { Assertions.assertEquals("Ugyldig token", thrown.message) }
        )
    }

    @Test
    fun testAuthenticateOK() {
        val request = AuthenticationRequest(
                registerRequest.email,
                registerRequest.password
        )
        val authentication: Authentication = UsernamePasswordAuthenticationToken(
                registerRequest.email,
                registerRequest.password
        )
        every { authenticationManager.authenticate(authentication) } returns authentication
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        val authenticationResponse = testSubject.authenticate(request)

        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNotNull(authenticationResponse.token) },
                { Assertions.assertNotNull(authenticationResponse.user) },
                { Assertions.assertNotNull(authenticationResponse.user?.role) },
                { Assertions.assertNull(authenticationResponse.message) },
                { Assertions.assertEquals(userEntity.firstName, authenticationResponse.user?.firstName) },
                { Assertions.assertEquals(userEntity.middleName, authenticationResponse.user?.middleName) },
                { Assertions.assertEquals(userEntity.lastName, authenticationResponse.user?.lastName) },
                { Assertions.assertEquals(userEntity.email, authenticationResponse.user?.email) },
                {
                    Assertions.assertEquals(
                            userEntity.role.name.name, authenticationResponse.user?.role?.name?.name
                    )
                },
                { Assertions.assertEquals(userEntity.area, authenticationResponse.user?.area) }
        )
    }

    @Test
    fun testAuthenticateBadCredentials() {
        val request = AuthenticationRequest(
                registerRequest.email,
                registerRequest.password
        )
        val authentication: Authentication = UsernamePasswordAuthenticationToken(request.email, request.password)
        every { authenticationManager.authenticate(authentication) } throws BadCredentialsException("Bad Credentials")
        val authenticationResponse = AtomicReference<AuthenticationResponse>()
        val thrown = assertThrows<BadCredentialsException> { authenticationResponse.set(testSubject.authenticate(request)) }
        Assertions.assertAll(
                { Assertions.assertNull(authenticationResponse.get()) },
                { Assertions.assertNotNull(thrown) },
                { Assertions.assertEquals("Bad Credentials", thrown.message) }
        )
    }

    @Test
    fun testAuthenticateNoValidToken() {
        val request = AuthenticationRequest(
                registerRequest.email,
                registerRequest.password
        )
        val authentication: Authentication = UsernamePasswordAuthenticationToken(
                registerRequest.email,
                registerRequest.password
        )
        every { authenticationManager.authenticate(authentication) } returns authentication
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntityNoValidToken
        val authenticationResponse = AtomicReference<AuthenticationResponse>()
        val thrown = assertThrows<InvalidTokenException> { authenticationResponse.set(testSubject.authenticate(request)) }
        Assertions.assertAll(
                { Assertions.assertNull(authenticationResponse.get()) },
                { Assertions.assertNotNull(thrown) },
                { Assertions.assertEquals("Ingen gyldig token", thrown.message) }
        )
    }

    @Test
    fun testRefresh() {
        val token = buildToken(
                extraClaims = emptyMap<String, Any>(),
                username = authorityEntityUser.name,
                authorities = listOf(SimpleGrantedAuthority(RoleNameEntity.ROLE_USER.name)),
                jwtKey = jwtKey,
                jwtExpiresTimeoutMs = jwtExpiresTimeoutMs
        )
        every { request.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer $token"
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity

        val authenticationResponse = testSubject.refresh(request)

        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNotNull(authenticationResponse.token) },
                { Assertions.assertNotNull(authenticationResponse.user) },
                { Assertions.assertNotNull(authenticationResponse.user?.role) },
                { Assertions.assertNull(authenticationResponse.message) },
                { Assertions.assertEquals(userEntity.firstName, authenticationResponse.user?.firstName) },
                { Assertions.assertEquals(userEntity.middleName, authenticationResponse.user?.middleName) },
                { Assertions.assertEquals(userEntity.lastName, authenticationResponse.user?.lastName) },
                { Assertions.assertEquals(userEntity.email, authenticationResponse.user?.email) },
                {
                    Assertions.assertEquals(
                            userEntity.role.name.name,
                            authenticationResponse.user?.role?.name?.name
                    )
                },
                { Assertions.assertEquals(userEntity.area, authenticationResponse.user?.area) }
        )
    }

    @Test
    fun testRefreshNoToken() {
        every { request.getHeader(HttpHeaders.AUTHORIZATION) } returns null
        val authenticationResponse = testSubject.refresh(request)
        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNull(authenticationResponse.token) },
                { Assertions.assertNull(authenticationResponse.user) },
                { Assertions.assertNull(authenticationResponse.message) },
        )
    }

    @Test
    fun testRefreshWrongAuthorization() {
        every { request.getHeader(HttpHeaders.AUTHORIZATION) } returns "HEI"
        val authenticationResponse: AuthenticationResponse = testSubject.refresh(request)
        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNull(authenticationResponse.token) },
                { Assertions.assertNull(authenticationResponse.user) },
                { Assertions.assertNull(authenticationResponse.message) },
        )
    }

    @Test
    fun testRefreshIllegalToken() {
        every { request.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer zzz.yyy.xxx"
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { jwtService.extractUsername(any()) } returns userEntity.email
        every { jwtService.isTokenValid(any(), any()) } throws MalformedJwtException("Malformed protected header JSON: Unable to deserialize: Unexpected character")
        val thrown = assertThrows<MalformedJwtException> { testSubject.refresh(request) }
        Assertions.assertAll(
                { Assertions.assertNotNull(thrown) },
                { assertTrue(thrown.message!!.startsWith("Malformed protected header JSON: Unable to deserialize: Unexpected character")) }
        )
    }

    @Test
    fun testFacebookAuthenticateExistingUser() {
        val facebookAuthenticationRequest = FacebookAuthenticationRequest(
                id = "1",
                firstName = userEntity.firstName,
                middleName = userEntity.middleName,
                lastName = userEntity.lastName,
                email = userEntity.email,
                picture = FacebookPicture(FacebookPictureData(url = "https://example.com", height = 50, width = 50, isSilhouette = false))
        )
        every { userRepository.findByFacebookUserId(any()) } returns userEntity
        every { userService.update(any(), any()) } returns userMapper.entityToModel(userEntity)
        every { authenticationManager.authenticate(any()) } returns UsernamePasswordAuthenticationToken("", "")
        every { userRepository.save(any()) } returns userEntity
        every { jwtService.generateToken(any()) } returns "token"

        val authenticationResponse: AuthenticationResponse = testSubject.facebookAuthenticate(facebookAuthenticationRequest)
        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNotNull(authenticationResponse.token) },
                { Assertions.assertNotNull(authenticationResponse.user) },
                { Assertions.assertNull(authenticationResponse.message) }
        )
    }

    @Test
    fun testFacebookAuthenticateExistingUserNoImage() {
        val facebookAuthenticationRequest = FacebookAuthenticationRequest(
                id = "1",
                firstName = userEntity.firstName,
                middleName = userEntity.middleName,
                lastName = userEntity.lastName,
                email = userEntity.email,
                picture = null
        )

        every { userRepository.findByFacebookUserId(any()) } returns userEntity
        every { userService.update(any(), any()) } returns userMapper.entityToModel(userEntity)
        every { authenticationManager.authenticate(any()) } returns UsernamePasswordAuthenticationToken("", "")
        every { userRepository.save(any()) } returns userEntity
        every { jwtService.generateToken(any()) } returns "token"

        val authenticationResponse: AuthenticationResponse = testSubject.facebookAuthenticate(facebookAuthenticationRequest)
        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNotNull(authenticationResponse.token) },
                { Assertions.assertNotNull(authenticationResponse.user) },
                { Assertions.assertNull(authenticationResponse.message) }
        )
    }

    @Test
    fun testFacebookAuthenticateNewUser() {
        val facebookAuthenticationRequest = FacebookAuthenticationRequest(
                "1",
                userEntity.firstName,
                userEntity.middleName,
                userEntity.lastName,
                userEntity.email,
                null
        )

        every { userRepository.findByFacebookUserId(any()) } returns null
        every { userRepository.findByEmailIgnoreCase(any()) } returns null
        every { userRepository.findAll() } returns listOf(userEntity)
        every { roleRepository.findRoleEntityByName(any()) } returns roleEntityUser
        every { userService.create(any()) } returns userMapper.entityToModel(userEntity)
        every { jwtService.generateToken(any()) } returns "token"

        val authenticationResponse = testSubject.facebookAuthenticate(facebookAuthenticationRequest)

        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNotNull(authenticationResponse.token) },
                { Assertions.assertNotNull(authenticationResponse.user) },
                { Assertions.assertNull(authenticationResponse.message) }
        )
    }

    @Test
    fun testGoogleAuthenticateExistingUser() {
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntityAdmin
        every { authenticationManager.authenticate(any()) } returns UsernamePasswordAuthenticationToken("", "")
        every { userRepository.save(any()) } returns userEntityAdmin
        every { jwtService.generateToken(any()) } returns "token"

        val authenticationRequest = GoogleAuthenticationRequest("", "", "", "", null)
        val authenticationResponse = testSubject.googleAuthenticate(authenticationRequest)

        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNotNull(authenticationResponse.token) },
                { Assertions.assertNotNull(authenticationResponse.user) },
                { Assertions.assertNull(authenticationResponse.message) }
        )
    }

    @Test
    fun testGoogleAuthenticateNewUser() {
        every { userRepository.findByEmailIgnoreCase(any()) } returns null
        every { userRepository.findAll() } returns listOf()
        every { roleRepository.findRoleEntityByName(any()) } returns roleEntityUser
        every { userService.create(any()) } returns userMapper.entityToModel(userEntity)
        every { jwtService.generateToken(any()) } returns "token"

        val authenticationRequest = GoogleAuthenticationRequest("", "", "", "", null)
        val authenticationResponse = testSubject.googleAuthenticate(authenticationRequest)

        Assertions.assertAll(
                { Assertions.assertNotNull(authenticationResponse) },
                { Assertions.assertNotNull(authenticationResponse.token) },
                { Assertions.assertNotNull(authenticationResponse.user) },
                { Assertions.assertNull(authenticationResponse.message) }
        )
    }

    @Test
    fun testAddtoken() {
        val email = userEntity.email
        val token = "TOKEN"

        every { userRepository.findByEmailIgnoreCase(email) } returns userEntity
        every { calendarTokenRepository.findCalendarTokenByToken(any()) } returns calendarTokenEntity
        every { userRepository.save(any()) } returns userEntity
        try {
            testSubject.addtoken(AddTokenRequest(email, token))
        } catch (e: Exception) {
            fail("Should not throw exception, but got ${e.javaClass.simpleName}")
        }
    }

    @Test
    fun testAddtokenNoUser() {
        val email = userEntity.email
        val token = "TOKEN"

        every { userRepository.findByEmailIgnoreCase(email) } returns null

        val thrown = assertThrows<UsernameNotFoundException> { testSubject.addtoken(AddTokenRequest(email, token)) }
        Assertions.assertAll(
                { Assertions.assertNotNull(thrown) },
                { Assertions.assertEquals("User not found", thrown.message) }
        )
    }
}