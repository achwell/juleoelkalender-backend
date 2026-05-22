package no.juleoelkalender.service.impl

import jakarta.mail.MessagingException
import jakarta.servlet.http.HttpServletRequest
import no.juleoelkalender.config.JwtService
import no.juleoelkalender.config.MailProperties
import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.entity.RoleNameEntity
import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.exception.InvalidTokenException
import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.exception.UserExistException
import no.juleoelkalender.mappers.RoleMapper
import no.juleoelkalender.mappers.UserMapper
import no.juleoelkalender.model.AddTokenRequest
import no.juleoelkalender.model.AuthenticationRequest
import no.juleoelkalender.model.AuthenticationResponse
import no.juleoelkalender.model.CalendarToken
import no.juleoelkalender.model.RegisterRequest
import no.juleoelkalender.model.User
import no.juleoelkalender.model.externalauth.FacebookAuthenticationRequest
import no.juleoelkalender.model.externalauth.GoogleAuthenticationRequest
import no.juleoelkalender.repository.CalendarTokenRepository
import no.juleoelkalender.repository.RoleRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.AuthenticationService
import no.juleoelkalender.service.EmailService
import no.juleoelkalender.service.UserService
import no.juleoelkalender.utils.ResourceReader.asString
import org.apache.commons.lang3.RandomStringUtils
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.core5.http.HttpStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.io.IOException
import java.time.ZonedDateTime

@Service
class AuthenticationServiceImpl(
    private val userRepository: UserRepository,
    private val calendarTokenRepository: CalendarTokenRepository, private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder, private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager, private val userMapper: UserMapper, private val roleMapper: RoleMapper,
    private val emailService: EmailService, private val userService: UserService, private val mailProperties: MailProperties,
    @param:Value("classpath:emails/welcome.html") private val welcomeEmail: Resource
) : AuthenticationService {

    @Throws(MessagingException::class)
    override fun register(request: RegisterRequest): AuthenticationResponse {
        val now = ZonedDateTime.now()
        val allUsers = userRepository.findAll()
        val isFirstUser = allUsers.isEmpty()
        val existingUser = allUsers.firstOrNull { it.email.equals(request.email, ignoreCase = true) }
        val calendarToken = requireCalendarToken(request.calendarToken)

        val savedUser = existingUser?.let { addCalendarTokenToExistingUser(it, calendarToken) }
            ?: createRegisteredUser(request, calendarToken, isFirstUser, now)

        val user = toUserWithActiveTokens(savedUser)
        sendWelcomeEmail(user, now)
        return responseWithToken(user)
    }

    override fun authenticate(request: AuthenticationRequest): AuthenticationResponse {
        authenticateCredentials(request.email, request.password)
        val userEntity = findUserByEmail(request.email)
        requireActiveCalendarToken(userEntity)
        userEntity.lastLoginDate = ZonedDateTime.now()
        return responseWithToken(toUserWithActiveTokens(userRepository.save(userEntity)))
    }

    override fun facebookAuthenticate(request: FacebookAuthenticationRequest): AuthenticationResponse {
        val now = ZonedDateTime.now()
        val existingUser = getExistingUser(request)

        if (existingUser != null) {
            requireActiveCalendarToken(existingUser)
            authenticateCredentials(existingUser.email, existingUser.pwd)
            handleFacebookPicture(request, existingUser)
            return responseWithToken(saveFacebookLogin(existingUser, now))
        }

        val role = requireRole(if (isFirstUser()) RoleNameEntity.ROLE_MASTER else RoleNameEntity.ROLE_USER)
        val user = User(
            null, request.firstName, request.middleName,
            request.lastName, request.email, "", null, roleMapper.entityToModel(role),
            false,
            mutableSetOf(), mutableSetOf(), null, now, request.id, null, null, null, false
        )
        handleFacebookPicture(request, user)
        return completeExternalRegistration(user)
    }

    override fun googleAuthenticate(request: GoogleAuthenticationRequest): AuthenticationResponse {
        val now = ZonedDateTime.now()
        val userEntity = userRepository.findByEmailIgnoreCase(request.email)

        if (userEntity != null) {
            requireActiveCalendarToken(userEntity)
            authenticateCredentials(userEntity.email, userEntity.password)
            userEntity.lastLoginDate = now
            userEntity.updatedDate = now
            return responseWithToken(toUserWithActiveTokens(userRepository.save(userEntity)))
        }

        val role = requireRole(if (isFirstUser()) RoleNameEntity.ROLE_MASTER else RoleNameEntity.ROLE_USER)
        val user = User(
            null, request.givenName, null, request.familyName,
            request.email, "", null, roleMapper.entityToModel(role), false, mutableSetOf(), mutableSetOf(),
            null, now, null, null, null, null, false
        )
        user.imageUrl = request.picture
        return completeExternalRegistration(user)
    }

    override fun refresh(request: HttpServletRequest): AuthenticationResponse {
        val token = extractBearerToken(request) ?: return emptyAuthenticationResponse()
        val userEmail = jwtService.extractUsername(token) ?: throw BadCredentialsException(INVALID_TOKEN_MESSAGE)
        val userEntity = userRepository.findByEmailIgnoreCase(userEmail) ?: return emptyAuthenticationResponse()

        requireActiveCalendarToken(userEntity)
        val user = toUserWithActiveTokens(userEntity)
        return if (jwtService.isTokenValid(token, user)) {
            responseWithToken(user)
        } else {
            emptyAuthenticationResponse()
        }
    }

    override fun addtoken(request: AddTokenRequest) {
        val userEntity = findUserByEmail(request.email)
        val token = calendarTokenRepository.findCalendarTokenByToken(request.token)!!
        userEntity.calendarToken.add(token)
        userRepository.save(userEntity)
    }

    private fun createRegisteredUser(
        request: RegisterRequest,
        calendarToken: CalendarTokenEntity,
        isFirstUser: Boolean,
        now: ZonedDateTime
    ): UserEntity {
        val role = requireRole(if (isFirstUser) RoleNameEntity.ROLE_MASTER else RoleNameEntity.ROLE_USER)
        return userRepository.save(
            UserEntity(
                id = null,
                firstName = request.firstName,
                middleName = request.middleName,
                lastName = request.lastName,
                email = request.email,
                password = passwordEncoder.encode(request.password)!!,
                area = request.area,
                role = role,
                locked = false,
                beers = mutableSetOf(),
                devices = mutableSetOf(),
                calendarToken = mutableSetOf(calendarToken),
                reviews = mutableSetOf(),
                lastLoginDate = null,
                createdDate = now,
                updatedDate = now,
                facebookUserId = null,
                imageUrl = null,
                imageHeight = null,
                imageWidth = null,
                imageSilhouette = false
            )
        )
    }

    private fun addCalendarTokenToExistingUser(userEntity: UserEntity, calendarToken: CalendarTokenEntity): UserEntity {
        if (userEntity.calendarToken.none { it.id == calendarToken.id }) {
            userEntity.calendarToken.add(calendarToken)
            return userRepository.save(userEntity)
        }
        throw UserExistException("Brukeren finnes allerede")
    }

    private fun sendWelcomeEmail(user: User, now: ZonedDateTime) {
        val mailContent = asString(welcomeEmail)
            .replace($$"${base_url}", mailProperties.baseUrl!!)
            .replace($$"${support_email}", mailProperties.supportEmail!!)
            .replace($$"${calendar_token_name}", user.calendarToken.first().name)
            .replace($$"${year}", now.year.toString())

        emailService.sendSimpleMessage(
            mailProperties.from!!,
            user.email,
            "Velkommen til Juleølkalender!",
            mailContent
        )
    }

    private fun responseWithToken(user: User): AuthenticationResponse {
        return AuthenticationResponse(jwtService.generateToken(user), user, null)
    }

    private fun emptyAuthenticationResponse(): AuthenticationResponse {
        return AuthenticationResponse(null, null, null)
    }

    private fun authenticateCredentials(email: String, password: String) {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
    }

    private fun findUserByEmail(email: String): UserEntity {
        return userRepository.findByEmailIgnoreCase(email)
            ?: throw UsernameNotFoundException("User not found")
    }

    private fun requireCalendarToken(token: String): CalendarTokenEntity {
        return calendarTokenRepository.findCalendarTokenByToken(token)
            ?: throw InvalidTokenException("Ugyldig token")
    }

    private fun requireRole(roleName: RoleNameEntity) = roleRepository.findRoleEntityByName(roleName)
        ?: throw NotFoundException("Role $roleName not found")

    private fun isFirstUser(): Boolean = userRepository.findAll().isEmpty()

    private fun requireActiveCalendarToken(userEntity: UserEntity) {
        if (userEntity.calendarToken.none { it.active }) {
            throw InvalidTokenException(NO_ACTIVE_TOKEN_MESSAGE)
        }
    }

    private fun requireActiveCalendarToken(user: User) {
        if (user.calendarToken.none { it.active }) {
            throw InvalidTokenException(NO_ACTIVE_TOKEN_MESSAGE)
        }
    }

    private fun toUserWithActiveTokens(userEntity: UserEntity): User {
        val user = userMapper.entityToModel(userEntity)
        user.calendarToken = user.calendarToken.filter(CalendarToken::active).toSet()
        return user
    }

    private fun saveFacebookLogin(user: User, now: ZonedDateTime): User {
        val savedUser = userRepository.save(userMapper.modelToEntity(user).apply {
            lastLoginDate = now
            updatedDate = now
            imageUrl = user.imageUrl
            imageHeight = user.imageHeight
            imageWidth = user.imageWidth
            imageSilhouette = user.imageSilhouette
        })
        return toUserWithActiveTokens(savedUser)
    }

    private fun completeExternalRegistration(user: User): AuthenticationResponse {
        val now = ZonedDateTime.now()
        val newUser = userService.create(
            user.copy(
                calendarToken = mutableSetOf(),
                pwd = RandomStringUtils.secureStrong().next(12),
                locked = false,
                lastLoginDate = now,
                createdDate = now,
            )
        )
        return responseWithToken(newUser)
    }

    private fun extractBearerToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        if (!authHeader.startsWith("Bearer ")) {
            return null
        }
        return authHeader.substring(7)
    }

    private fun getExistingUser(request: FacebookAuthenticationRequest): User? {
        userRepository.findByFacebookUserId(request.id)?.let { return updateUser(request, it) }
        userRepository.findByEmailIgnoreCase(request.email)?.let { return updateUser(request, it) }
        return null
    }

    private fun updateUser(request: FacebookAuthenticationRequest, user: UserEntity): User? {
        val data = request.picture?.data
        val updatedUser = userMapper.entityToModel(user.apply {
            firstName = request.firstName
            middleName = request.middleName
            lastName = request.lastName
            facebookUserId = request.id
            imageUrl = data?.url
            imageHeight = data?.height
            imageWidth = data?.width
            imageSilhouette = data?.isSilhouette ?: false
        })
        return userService.update(user.id!!, updatedUser)
    }

    private fun handleFacebookPicture(request: FacebookAuthenticationRequest, user: User) {
        val data = request.picture?.data ?: return
        val currentImageUrl = user.imageUrl

        try {
            HttpClientBuilder.create().build().use { client ->
                val httpGet = HttpGet(data.url)
                client.execute(httpGet) { response ->
                    if (response.code != HttpStatus.SC_NOT_FOUND) {
                        user.imageUrl = data.url
                        user.imageHeight = data.height
                        user.imageWidth = data.width
                        user.imageSilhouette = data.isSilhouette
                    } else if (currentImageUrl == null || "fbsbx" in currentImageUrl) {
                        user.imageUrl = null
                        user.imageHeight = null
                        user.imageWidth = null
                        user.imageSilhouette = false
                    }
                    response
                }
            }
        } catch (e: IOException) {
            log.error("Error getting facebook image", e)
        }
    }

    companion object {
        private const val INVALID_TOKEN_MESSAGE = "Invalid Token received!"
        private const val NO_ACTIVE_TOKEN_MESSAGE = "Ingen gyldig token"
        private val log: Logger = LoggerFactory.getLogger(AuthenticationServiceImpl::class.java)
    }
}
