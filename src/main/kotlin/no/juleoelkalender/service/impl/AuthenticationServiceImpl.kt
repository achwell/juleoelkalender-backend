package no.juleoelkalender.service.impl

import jakarta.mail.MessagingException
import jakarta.servlet.http.HttpServletRequest
import no.juleoelkalender.config.JwtService
import no.juleoelkalender.config.MailProperties
import no.juleoelkalender.entity.RoleNameEntity
import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.exception.InvalidTokenException
import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.exception.UserExistException
import no.juleoelkalender.mappers.RoleMapper
import no.juleoelkalender.mappers.UserMapper
import no.juleoelkalender.model.*
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
        val allUsers = userRepository.findAll()
        val firstUser = allUsers.isEmpty()
        var userEntity: UserEntity? = null
        if (!firstUser) {
            userEntity = allUsers.firstOrNull { it.email.equals(request.email, ignoreCase = true) }
        }
        val token = calendarTokenRepository.findCalendarTokenByToken(request.calendarToken)
                ?: throw InvalidTokenException("Ugyldig token")
        val now = ZonedDateTime.now()
        if (userEntity == null) {
            val role = roleRepository.findRoleEntityByName(
                    if (firstUser) RoleNameEntity.ROLE_MASTER else RoleNameEntity.ROLE_USER
            )
            userEntity = userRepository.save(
                    UserEntity(
                            id = null,
                            firstName = request.firstName,
                            middleName = request.middleName,
                            lastName = request.lastName,
                            email = request.email,
                            password = passwordEncoder.encode(request.password)!!,
                            area = request.area,
                            role = role!!,
                            locked = false,
                            beers = mutableSetOf(),
                            devices = mutableSetOf(),
                            calendarToken = mutableSetOf(token),
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
        } else {
            val newToken = userEntity.calendarToken.any { it.id != token.id }
            if (newToken) {
                userEntity.calendarToken.add(token)
                userEntity = userRepository.save(userEntity)
            } else {
                throw UserExistException("Brukeren finnes allerede")
            }
        }
        val user = userMapper.entityToModel(userEntity)
        val calendarTokens = user.calendarToken.filter(CalendarToken::active).toSet()
        user.calendarToken = calendarTokens
        val jwtToken = jwtService.generateToken(user)
        val mailContent = asString(welcomeEmail)
                .replace($$"${base_url}", mailProperties.baseUrl!!)
                .replace($$"${support_email}", mailProperties.supportEmail!!)
                .replace($$"${calendar_token_name}", user.calendarToken.first().name)
                .replace($$"${year}", now.year.toString())
        emailService.sendSimpleMessage(
                mailProperties.from!!, user.email,
                "Velkommen til Juleølkalender!", mailContent
        )
        return AuthenticationResponse(jwtToken, user, null)
    }

    override fun authenticate(request: AuthenticationRequest): AuthenticationResponse {
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                        request.email,
                        request.password
                )
        )
        val userEntity = userRepository.findByEmailIgnoreCase(request.email)
                ?: throw UsernameNotFoundException("User not found")
        if (userEntity.calendarToken.none { it.active }) {
            throw InvalidTokenException("Ingen gyldig token")
        }
        userEntity.lastLoginDate = ZonedDateTime.now()
        userRepository.save(userEntity)
        val user = userMapper.entityToModel(userEntity)
        val calendarTokens = user.calendarToken.filter { it.active }.toSet()
        user.calendarToken = calendarTokens
        val jwtToken = jwtService.generateToken(user)
        return AuthenticationResponse(jwtToken, user, null)
    }

    override fun facebookAuthenticate(request: FacebookAuthenticationRequest): AuthenticationResponse {
        var user = getExistingUser(request)
        val now = ZonedDateTime.now()
        if (user != null) {
            if (user.calendarToken.none { it.active }) {
                throw InvalidTokenException("Ingen gyldig token")
            }
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(user.email, user.pwd)
            )
            handleFacebookPicture(request, user)
            val userEntity = userMapper.modelToEntity(user).apply {
                lastLoginDate = now
                updatedDate = now
                imageUrl = user.imageUrl
                imageHeight = user.imageHeight
                imageWidth = user.imageWidth
                imageSilhouette = user.imageSilhouette
            }
            userRepository.save(userEntity)
            val calendarTokens = user.calendarToken.filter { it.active }.toSet()
            user.calendarToken = calendarTokens
            val jwtToken = jwtService.generateToken(user)
            return AuthenticationResponse(jwtToken, user, null)
        }
        val allUsers = userRepository.findAll()
        val firstUser = allUsers.isEmpty()
        val roleName = if (firstUser) RoleNameEntity.ROLE_MASTER else RoleNameEntity.ROLE_USER
        val role = roleRepository.findRoleEntityByName(roleName)
                ?: throw NotFoundException("Role $roleName not found")
        user = User(
                null, request.firstName, request.middleName,
                request.lastName, request.email, "", null, roleMapper.entityToModel(role),
                false,
                mutableSetOf(), mutableSetOf(), null, now, request.id, null, null, null, false
        )
        handleFacebookPicture(request, user)
        return setDefaultValues(user)
    }

    override fun googleAuthenticate(request: GoogleAuthenticationRequest): AuthenticationResponse {
        val userEntity = userRepository.findByEmailIgnoreCase(request.email)
        val now = ZonedDateTime.now()
        if (userEntity != null) {
            if (userEntity.calendarToken.none { it.active }) {
                throw InvalidTokenException("Ingen gyldig token")
            }
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(userEntity.email, userEntity.password)
            )
            userEntity.lastLoginDate = now
            userEntity.updatedDate = now
            userRepository.save(userEntity)
            val calendarTokens = userEntity.calendarToken.filter { it.active }.toMutableSet()
            userEntity.calendarToken = calendarTokens
            val user = userMapper.entityToModel(userEntity)
            val jwtToken = jwtService.generateToken(user)
            return AuthenticationResponse(jwtToken, user, null)
        }
        val allUsers = userRepository.findAll()
        val firstUser = allUsers.isEmpty()
        val role = roleRepository.findRoleEntityByName(
                if (firstUser) RoleNameEntity.ROLE_MASTER else RoleNameEntity.ROLE_USER
        )
        val user = User(
                null, request.givenName, null, request.familyName,
                request.email, "", null, roleMapper.entityToModel(role!!), false, mutableSetOf(), mutableSetOf(),
                null, now, null, null, null, null, false
        )
        val picture = request.picture

        if (picture != null) {
            user.imageUrl = picture
        }
        return setDefaultValues(user)
    }

    override fun refresh(request: HttpServletRequest): AuthenticationResponse {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        val userEmail: String?
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return AuthenticationResponse(null, null, "Invalid token")
        }
        val token: String = authHeader.substring(7)
        userEmail = jwtService.extractUsername(token)
        if (userEmail != null) {
            val userEntity = userRepository.findByEmailIgnoreCase(userEmail)
            if (userEntity != null) {
                if (userEntity.calendarToken.none { it.active }) {
                    throw InvalidTokenException("Ingen gyldig token")
                }
                val user = userMapper.entityToModel(userEntity)
                user.calendarToken = user.calendarToken.filter { it.active }.toSet()
                if (jwtService.isTokenValid(token, user)) {
                    val accessToken = jwtService.generateToken(user)
                    return AuthenticationResponse(accessToken, user, null)
                }
            }
        } else {
            throw BadCredentialsException("Invalid Token received!")
        }
        return AuthenticationResponse(null, null, null)
    }

    override fun addtoken(request: AddTokenRequest) {
        val userEntity = userRepository.findByEmailIgnoreCase(request.email)
                ?: throw UsernameNotFoundException("User not found")
        val token = calendarTokenRepository.findCalendarTokenByToken(request.token)
        userEntity.calendarToken.add(token!!)
        userRepository.save(userEntity)
    }

    private fun setDefaultValues(user: User): AuthenticationResponse {
        val now = ZonedDateTime.now()
        val newUser = userService.create(user.copy(
                calendarToken = mutableSetOf(),
                pwd = RandomStringUtils.secureStrong().next(12),
                locked = false,
                lastLoginDate = now,
                createdDate = now,
        ))
        return AuthenticationResponse(null, newUser, null)
    }

    private fun getExistingUser(request: FacebookAuthenticationRequest): User? {
        val facebookId = request.id
        val email = request.email
        var optionalUser = userRepository.findByFacebookUserId(facebookId)
        if (optionalUser != null) {
            return updateUser(request, optionalUser)
        } else {
            optionalUser = userRepository.findByEmailIgnoreCase(email)
            if (optionalUser != null) {
                return updateUser(request, optionalUser)
            }
        }
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
        val data = request.picture?.data

        if (data != null) {
            val imageUrl = user.imageUrl
            try {
                HttpClientBuilder.create().build().use { client ->
                    val httpGet = HttpGet(data.url)
                    client.execute(httpGet) { response ->
                        if (response.code != HttpStatus.SC_NOT_FOUND) {
                            user.imageUrl = data.url
                            user.imageHeight = data.height
                            user.imageWidth = data.width
                            user.imageSilhouette = data.isSilhouette
                        } else if (imageUrl == null || "fbsbx" in imageUrl) {
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
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AuthenticationServiceImpl::class.java)

    }
}
